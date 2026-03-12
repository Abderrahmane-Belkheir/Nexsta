package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.CommentRequest;
import com.example.SocialMediaApp.Content.api.dto.CommentResponse;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.domain.*;
import com.example.SocialMediaApp.Content.persistence.*;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.Upload.Exceptions.UnauthorizedResourceAccessException;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostInteractionService {

    private final PostRepo postRepo;
    private final PostLikeRepo postLikeRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final CommentRepo commentRepo;
    private final Contentmapper contentmapper;

    // toggle between Post liked and not liked
    public LikeResponse addPostLike(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post post=postRepo.findById(postId).orElseThrow(()-> new ContentNotFoundException("Post Not Found"));

        boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,post.getUserId());

        if(!isAllowed) throw new ActionNotAllowedException("Action could not be completed");

        boolean liked=postLikeRepo.existsByPostIdAndUserId(postId,currentUserId);

        if(liked){
            postLikeRepo.deleteByPostIdAndUserId(postId,currentUserId);
            postRepo.decrementPostLikes(postId);
        }else{
            postLikeRepo.save(new PostLike(currentUserId,postId));
            postRepo.incrementPostLikes(postId);
            // handling notifications later
        }
        return new LikeResponse(!liked);
    }


    public CommentResponse addPostComment(String postId, CommentRequest commentRequest){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post post=postRepo.findById(postId).orElseThrow(()-> new ContentNotFoundException("Post Not Found"));

        String postOwnerId=post.getUserId();

        PostSettings postSettings=post.getPostSettings();

        boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,postOwnerId)&&!postSettings.isCommentsDisabled();

        if(!isAllowed) throw new ActionNotAllowedException("Action could not be completed");

        Comment comment=commentRepo.save(new Comment(commentRequest.getContent(),currentUserId,postId,postOwnerId));

        postRepo.incrementPostComments(postId);
        // handling notification later
        return contentmapper.toCommentResponse(comment);
    }

    public void removePostComment(String commentId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Comment comment=commentRepo.findById(commentId).orElseThrow(()->new ContentNotFoundException("Comment Not Found"));
        boolean isAllowed=comment.getUserId().equals(currentUserId);
        if(!isAllowed) throw new UnauthorizedResourceAccessException("Action could not be completed");
        int updated=commentRepo.deleteByIdAndUserId(commentId,currentUserId);
        if(updated==0){
            throw new ActionNotAllowedException("Unable to remove comment : "+commentId);
        }
        String postId=comment.getPostId();
        postRepo.decrementPostComments(postId);
    }

}
