package com.example.SocialMediaApp.Content.application;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.*;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Scheduling.application.ContentSchedulingService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostLifecycleService {

    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final MediaLifecycleService mediaLifecycleService;
    private final Contentmapper contentmapper;
    private final StorageService storageService;
    private final ContentSchedulingService contentSchedulingService;
    private final StorageTransferManager storageTransferManager;
    private final MediaUrlResolver mediaUrlResolver;

    public PostRepresentation createPost(PostCreationRequest postCreation){
        String currentUserId=authenticatedUserService.getCurrentUser();
        List<String> uploadRequestsIds=postCreation.getUploadRequestsIds();
        UploadFinalization uploadFinalization=mediaLifecycleService.extractMediaUploads(currentUserId,uploadRequestsIds,UploadType.POST);
        String postId= UUID.randomUUID().toString();
        Post post= postRepo.save(Post.builder().id(postId).user(new User(currentUserId))
                .caption(postCreation.getCaption())
                .postSettings(postCreation.getPostSettings()).location(postCreation.getLocation()).build());
        String destinationFolder=mediaLifecycleService.buildFolderPath(currentUserId,postId,UploadType.POST);
        post.setPostFolderPath(destinationFolder);
        List<Media> mediaList=mediaLifecycleService.persistMedia(uploadFinalization.getMediaUploads(),post);
        storageService.transferTemporaryContent(destinationFolder,uploadFinalization.getFilePaths());
        PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
        postRepresentation.setPostStatus(Post.PostStatus.DRAFT);
        List<MediaRepresentation> mediaRepresentationList= mediaList.stream().map(media -> new MediaRepresentation(media.getId(),media.getMediaType())).toList();
        postRepresentation.getMediaList().addAll(mediaRepresentationList);
        return postRepresentation;
    }

    public void unSchedulePost(String postId) throws SchedulerException {
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserIdAndPostStatus(postId,currentUserId, Post.PostStatus.SCHEDULED).orElseThrow(()->new ContentNotFoundException("Post to unSchedule Not Found"));
        post.setScheduledAt(null);
        post.setPostStatus(Post.PostStatus.DRAFT);
        contentSchedulingService.unSchedulePostPublishing(postId);
        postRepo.save(post);
    }

    // publishing post for first time draft -> published / scheduled
    public void publishPost(PostPublish postPublish) throws SchedulerException {
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post draftPost=postRepo.findByIdAndUserIdAndPostStatusWithMediaList(postPublish.getPostId(),currentUserId).
                orElseThrow(()-> new ContentNotFoundException("Post Not Found"));

        if(draftPost.getPostStatus()!= Post.PostStatus.DRAFT) throw new ContentNotFoundException("Post Not Found");

        if(postPublish.getScheduledAt()!=null) {
            log.info("publishing : "+postPublish.getPostId()+" at : "+postPublish.getScheduledAt());
            draftPost.setPostStatus(Post.PostStatus.SCHEDULED);
            draftPost.setScheduledAt(postPublish.getScheduledAt());
            postRepo.save(draftPost);
            contentSchedulingService.schedulePostPublishing(postPublish);
            return;
        }
        StorageTransferManager.StorageTransfer storageTransfer=storageTransferManager.resolveStorageTransfer(Post.PostStatus.DRAFT, Post.PostStatus.PUBLISHED);
        String postFolder=draftPost.getPostFolderPath();
        storageService.moveBatchFiles(postFolder,storageTransfer);
        draftPost.setPublishedAt(Instant.now());
        draftPost.setPostStatus(Post.PostStatus.PUBLISHED);
        draftPost.setPostFolderPath(postFolder.replace(storageTransfer.getSourceDir().getDirName(),storageTransfer.getDestinationDir().getDirName()));
        postRepo.save(draftPost);
    }


    // switching between published <-> unpublished
    public PostVisibilityToggleResponse togglePostVisibility(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserId(postId,currentUserId).orElseThrow(()->new ContentNotFoundException("Post to Toggle Visibility Not Found"));
        StorageTransferManager.StorageTransfer storageTransfer;
        PostVisibilityToggleResponse.PostStatus responseStatus;
        if(post.getPostStatus()== Post.PostStatus.PUBLISHED){
            post.setPostStatus(Post.PostStatus.UNPUBLISHED);
            responseStatus= PostVisibilityToggleResponse.PostStatus.UNPUBLISHED;
            storageTransfer=storageTransferManager.resolveStorageTransfer(Post.PostStatus.PUBLISHED, Post.PostStatus.UNPUBLISHED);
        }else if (post.getPostStatus()== Post.PostStatus.UNPUBLISHED){
            post.setPostStatus(Post.PostStatus.PUBLISHED);
            responseStatus= PostVisibilityToggleResponse.PostStatus.PUBLISHED;
            storageTransfer=storageTransferManager.resolveStorageTransfer(Post.PostStatus.UNPUBLISHED, Post.PostStatus.PUBLISHED);
        }else {
            throw new ActionNotAllowedException(String.format("Post with Status %s Cannot be Toggled",post.getPostStatus()));
        }
        storageService.moveBatchFiles(post.getPostFolderPath(),storageTransfer);
        post.setPostFolderPath(post.getPostFolderPath().replace(storageTransfer.getSourceDir().getDirName(),storageTransfer.getDestinationDir().getDirName()));
        postRepo.save(post);
        return new PostVisibilityToggleResponse(responseStatus);
    }

    public DeletePostResponse deletePost(String postId) throws SchedulerException{
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserIdAndPostStatusWithMediaList(postId,currentUserId).orElseThrow(()->new ContentNotFoundException("Post to Delete Not Found"));
        if(post.getPostStatus()== Post.PostStatus.DELETED) throw new ActionNotAllowedException("Post Already Deleted");
        if(post.getPostStatus()== Post.PostStatus.SCHEDULED){
            post.setScheduledAt(null);
            contentSchedulingService.unSchedulePostPublishing(postId);
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