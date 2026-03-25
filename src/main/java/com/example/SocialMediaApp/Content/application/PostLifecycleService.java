package com.example.SocialMediaApp.Content.application;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.*;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Scheduling.application.ContentSchedulingService;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Storage.StorageDir;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.StorageTransfer;
import com.example.SocialMediaApp.Upload.domain.MediaUpload;
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

    public PostRepresentation createPost(PostCreationRequest postCreation){
        String currentUserId=authenticatedUserService.getCurrentUser();
        List<String> uploadRequestsIds=postCreation.getUploadRequestsIds();
        List<MediaUpload> mediaUploads=mediaLifecycleService.extractMediaUploads(currentUserId,uploadRequestsIds,UploadType.POST);
        Post post= postRepo.save(Post.builder().user(new User(currentUserId))
                .caption(postCreation.getCaption())
                .postSettings(postCreation.getPostSettings()).location(postCreation.getLocation()).build());
        List<Media> mediaList=mediaLifecycleService.persistMedia(mediaUploads,post);
        PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
        postRepresentation.setPostStatus(Post.PostStatus.DRAFT);
        List<MediaRepresentation> mediaRepresentationList= mediaList.stream().map(contentmapper::toMediaRepresentation).toList();
        postRepresentation.getMediaList().addAll(mediaRepresentationList);
        return postRepresentation;
    }

    // publishing post for first time draft -> published / scheduled
    public void publishPost(PostPublish postPublish) throws SchedulerException {
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post draftPost=postRepo.findByIdAndUserIdAndPostStatus(postPublish.getPostId(),currentUserId, Post.PostStatus.DRAFT).
                orElseThrow(()-> new ActionNotAllowedException("Action could not be completed"));
        if(postPublish.getScheduledAt()!=null) {
            draftPost.setPostStatus(Post.PostStatus.SCHEDULED);
            postRepo.save(draftPost);
            contentSchedulingService.schedulePostPublishing(currentUserId,postPublish);
            return;
        }
        draftPost.setPublishedAt(Instant.now());
        draftPost.setPostStatus(Post.PostStatus.PUBLISHED);
        postRepo.save(draftPost);
    }


    // switching between published <-> unpublished
    public PostVisibilityToggleResponse togglePostVisibility(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserId(postId,currentUserId).orElseThrow(()->new ContentNotFoundException("Post to Toggle Visibility Not Found"));

        if(post.getPostStatus()== Post.PostStatus.PUBLISHED){
            post.setPostStatus(Post.PostStatus.UNPUBLISHED);
            return new PostVisibilityToggleResponse(PostVisibilityToggleResponse.PostStatus.UNPUBLISHED);
        }else if (post.getPostStatus()== Post.PostStatus.UNPUBLISHED){
            post.setPostStatus(Post.PostStatus.PUBLISHED);
            return new PostVisibilityToggleResponse(PostVisibilityToggleResponse.PostStatus.PUBLISHED);
        }
        throw new ActionNotAllowedException("");
    }

    public DeletePostResponse deletePost(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserIdAndPostStatusWithMediaList(postId,currentUserId).orElseThrow(()->new ContentNotFoundException("Post to Delete Not Found"));
        if(post.getPostStatus()== Post.PostStatus.DELETED) throw new ActionNotAllowedException("Post Already Deleted");
        List<Media> mediaList=post.getMediaList();
        List<String> filePaths=mediaList.stream().map(Media::getFilepath).toList();
        if(post.isRestored()){
            try {
                storageService.deleteFiles(filePaths);
            } catch (Exception e) {
                log.error("Failed to delete files from storage for post {}: {}",
                        postId, e.getMessage());
                // continue with DB deletion regardless
            }
            postRepo.delete(post);
            return new DeletePostResponse(false);
        }else{
            StorageTransfer storageTransfer=new StorageTransfer(StorageDir.PERMANENT,StorageDir.DELETED);
            storageService.moveFiles(filePaths, storageTransfer);
            post.setPreDeletionStatus(post.getPostStatus());
            post.setPostStatus(Post.PostStatus.DELETED);
            post.setDeletedAt(Instant.now());
            mediaList.forEach(media -> media.transformFilePath(storageTransfer));
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
        List<String> filePaths=mediaList.stream().map(Media::getFilepath).toList();
        StorageTransfer storageTransfer=new StorageTransfer(StorageDir.DELETED,StorageDir.PERMANENT);
        storageService.moveFiles(filePaths,storageTransfer);
        mediaList.forEach(media -> media.transformFilePath(storageTransfer));
        // will save the media also thanks to cascading
        postRepo.save(post);
        PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
        postRepresentation.getMediaList().addAll(mediaList.stream().map(contentmapper::toMediaRepresentation).toList());
        postRepresentation.setLikes(post.getLikeCount());
        postRepresentation.setComments(post.getCommentCount());
        postRepresentation.setRestored(true);
        postRepresentation.setPostStatus(post.getPostStatus());
        return postRepresentation;
    }

}