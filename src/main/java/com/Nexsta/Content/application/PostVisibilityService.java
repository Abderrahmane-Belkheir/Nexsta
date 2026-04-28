package com.Nexsta.Content.application;

import com.Nexsta.Content.Exceptions.ContentNotFoundException;
import com.Nexsta.Content.api.dto.PostVisibilityToggleResponse;
import com.Nexsta.Content.domain.Post;
import com.Nexsta.Content.persistence.PostRepo;
import com.Nexsta.Shared.Exceptions.ActionNotAllowedException;
import com.Nexsta.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostVisibilityService {

    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final PostStorageService postStorageService;


    // switching between published <-> unpublished
    public PostVisibilityToggleResponse togglePostVisibility(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserId(postId,currentUserId).orElseThrow(()->new ContentNotFoundException("Post to Toggle Visibility Not Found"));
        PostVisibilityToggleResponse.PostStatus responseStatus;
        Post.PostStatus sourceStatus;
        if(post.getPostStatus()== Post.PostStatus.PUBLISHED){
            post.setPostStatus(Post.PostStatus.UNPUBLISHED);
            sourceStatus= Post.PostStatus.PUBLISHED;
            responseStatus= PostVisibilityToggleResponse.PostStatus.UNPUBLISHED;
        }else if (post.getPostStatus()== Post.PostStatus.UNPUBLISHED){
            post.setPostStatus(Post.PostStatus.PUBLISHED);
            sourceStatus= Post.PostStatus.UNPUBLISHED;
            responseStatus= PostVisibilityToggleResponse.PostStatus.PUBLISHED;
        }else {
            throw new ActionNotAllowedException(String.format("Post with Status %s Cannot be Toggled",post.getPostStatus()));
        }
        post.setPostFolderPath(postStorageService.moveAndResolvePath(post,sourceStatus,post.getPostStatus()));
        postRepo.save(post);
        return new PostVisibilityToggleResponse(responseStatus);
    }

}
