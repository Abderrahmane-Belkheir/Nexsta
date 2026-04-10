package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Configurations.Websocket.AuthChannelInterceptor;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.PostVisibilityToggleResponse;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.StorageTransferManager;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostVisibilityService {

    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final StorageTransferManager storageTransferManager;
    private final StorageService storageService;


    // switching between published <-> unpublished
    public PostVisibilityToggleResponse togglePostVisibility(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserId(postId,currentUserId).orElseThrow(()->new ContentNotFoundException("Post to Toggle Visibility Not Found"));
        StorageTransferManager.StorageTransfer storageTransfer;
        PostVisibilityToggleResponse.PostStatus responseStatus;
        if(post.getPostStatus()== Post.PostStatus.PUBLISHED){
            post.setPostStatus(Post.PostStatus.UNPUBLISHED);
            post.setPostPreview(null);
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

}
