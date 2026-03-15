package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.CommentRepresentation;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.CommentRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepo commentRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final PostRepo postRepo;

    private Page<CommentRepresentation> getCommentsRepresentations(String postId,int page){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndPostStatus(postId, Post.PostStatus.PUBLISHED).orElseThrow(()->new ContentNotFoundException("Post Not Found"));
        enforceCommentVisibility(post,currentUserId);





        return null;
    }

    public Page<CommentRepresentation> getPostComments(String postId,int page){

        return null;
    }

    public Page<CommentRepresentation> getCommentReplies(String commentId,int page){


        return null;
    }

    private void enforceCommentVisibility(Post post, String viewerId){

        String postOwnerId=post.getUserId();

        if(postOwnerId.equals(viewerId)) return;

        boolean isAllowed=visibilityPolicy.isAllowed(viewerId,postOwnerId);
        if(!isAllowed) throw new ContentNotAvailableException("");
        PostSettings postSettings=post.getPostSettings();
        if(postSettings.isHideComments()) throw new ContentNotAvailableException("Comments Are Hidden On This Post");

    }



}
