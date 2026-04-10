package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.DeletePostResponse;
import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostPreview;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.StorageTransferManager;
import com.example.SocialMediaApp.Upload.domain.UploadFinalization;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLifecycleService {

    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final MediaLifecycleService mediaLifecycleService;
    private final Contentmapper contentmapper;
    private final StorageService storageService;
    private final StorageTransferManager storageTransferManager;
    private final PostStorageService postStorageService;
    private final ThumbnailGenerator thumbnailGenerator;
    private final PostSchedulingService postSchedulingService;
    private final MediaUrlResolver mediaUrlResolver;

    public PostRepresentation createPost(PostCreationRequest request) throws SchedulerException {
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post.PostStatus status=getPostStatus(request.getPostAction());
        if(status== Post.PostStatus.DRAFT) validateDraftLimit(currentUserId);
        List<String> uploadRequestsIds=request.getUploadRequestsIds();
        UploadFinalization uploadFinalization=mediaLifecycleService.extractMediaUploads(currentUserId,uploadRequestsIds, UploadType.POST);
        String postId= UUID.randomUUID().toString();
        Post post= Post.builder().id(postId).postStatus(status).user(new User(currentUserId))
                .caption(request.getCaption())
                .postSettings(request.getPostSettings()).location(request.getLocation()).build();
        StorageTransferManager.StorageTransfer storageTransfer=storageTransferManager.resolveStorageTransfer(null,status);
        String destinationFolder=mediaLifecycleService.buildFolderPath(currentUserId,postId,storageTransfer.getDestinationDir(),UploadType.POST);
        post.setPostFolderPath(destinationFolder);
        List<Media> mediaList=mediaLifecycleService.persistMedia(uploadFinalization.getMediaUploads(),post);
        PostPreview postPreview=thumbnailGenerator.generatePostThumbnail(mediaList.get(0));
        post.setPostPreview(postPreview);
        postRepo.save(post);
        storageService.transferTemporaryContent(destinationFolder,uploadFinalization.getFilePaths(),storageTransfer);
        if(status== Post.PostStatus.SCHEDULED) postSchedulingService.schedulePost(postId,request.getScheduleAt());
        PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
        postRepresentation.setPostStatus(status);
        return postRepresentation;
    }

    private Post.PostStatus getPostStatus(PostCreationRequest.PostAction postAction){
        return switch (postAction){
            case DRAFT -> Post.PostStatus.DRAFT;
            case PUBLISHED -> Post.PostStatus.PUBLISHED;
            case SCHEDULED -> Post.PostStatus.SCHEDULED;
        };
    }

    private void validateDraftLimit(String userId){
        if(postRepo.isDraftLimitReached(userId,10))
            throw new ActionNotAllowedException("Draft Post limit exceeded Try to Delete Or Publish Some");
    }

public DeletePostResponse deletePost(String postId) throws SchedulerException {
    String currentUserId=authenticatedUserService.getCurrentUser();
    Post post=postRepo.findByIdAndUserIdAndPostStatusWithMediaList(postId,currentUserId).orElseThrow(()->new ContentNotFoundException("Post to Delete Not Found"));
    if(post.getPostStatus()== Post.PostStatus.DELETED) throw new ActionNotAllowedException("Post Already Deleted");
    if(post.getPostStatus()== Post.PostStatus.SCHEDULED){
        postSchedulingService.unSchedulePost(postId);
    }
    if(post.isRestored()){
        List<Media> mediaList=post.getMediaList();
        List<String> filePaths=mediaList.stream().map(media -> mediaUrlResolver.resolvePath(post.getPostFolderPath(),media.getId())).toList();
        try {
            storageService.deleteFiles(filePaths,storageTransferManager.resolveBucket(post.getPostStatus()));
        } catch (Exception e) {
            log.error("Failed to delete files from storage for post {}: {}",
                    postId, e.getMessage());
            // continue with DB deletion regardless
        }
        postRepo.delete(post);
        return new DeletePostResponse(false);
    }else{
        StorageTransferManager.StorageTransfer storageTransfer=storageTransferManager.resolveStorageTransfer(post.getPostStatus(),Post.PostStatus.DELETED);
        storageService.moveBatchFiles(post.getPostFolderPath(),storageTransfer);
        Post.PostStatus preDeletionStatus=post.getPostStatus()== Post.PostStatus.SCHEDULED? Post.PostStatus.DRAFT:post.getPostStatus();
        post.setPreDeletionStatus(preDeletionStatus);
        post.setPostStatus(Post.PostStatus.DELETED);
        post.setPostPreview(null);
        post.setDeletedAt(Instant.now());
        post.setPostFolderPath(post.getPostFolderPath().replace(storageTransfer.getSourceDir().getDirName(),storageTransfer.getDestinationDir().getDirName()));
        postRepo.save(post);
        return new DeletePostResponse(true);
    }
}

public PostRepresentation restorePost(String postId){
    String currentUserId=authenticatedUserService.getCurrentUser();
    Post post=postRepo.findPostToRestore(postId,currentUserId).
            orElseThrow(()->new ContentNotFoundException("Post to Restore Not Found"));
    post.setRestored(true);
    post.setPostStatus(post.getPreDeletionStatus());
    // not needed because post can only be restored if deleted once
    post.setPreDeletionStatus(null);
    List<Media> mediaList=post.getMediaList();
    StorageTransferManager.StorageTransfer storageTransfer=storageTransferManager.resolveStorageTransfer(Post.PostStatus.DELETED,post.getPostStatus());
    storageService.moveBatchFiles(post.getPostFolderPath(),storageTransfer);
    post.setPostFolderPath(post.getPostFolderPath().replace(storageTransfer.getSourceDir().getDirName(),storageTransfer.getDestinationDir().getDirName()));
    postRepo.save(post);
    PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
    postRepresentation.getMediaList().addAll(mediaList.stream().map(media -> new MediaRepresentation(media.getId(),media.getMediaType())).toList());
    postRepresentation.setLikes(post.getLikeCount());
    postRepresentation.setComments(post.getCommentCount());
    postRepresentation.setRestored(true);
    postRepresentation.setPostStatus(post.getPostStatus());
    return postRepresentation;
}

}


