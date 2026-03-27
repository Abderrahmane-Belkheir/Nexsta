package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.CommentCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.CommentRepresentation;
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

import java.util.Optional;

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
    private final LikeRepo likeRepo;
    // toggle between Post liked and not liked
    public LikeResponse addPostLike(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post post=postRepo.findById(postId).orElseThrow(()-> new ContentNotFoundException("Post Not Found"));

        boolean isOwner=post.getUserId().equals(currentUserId);

        if(!isOwner){
            boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,post.getUserId());

            if(!isAllowed) throw new ActionNotAllowedException("Post Not Found");

            if(post.getPostStatus()!= Post.PostStatus.PUBLISHED) throw new ContentNotAvailableException("Post Not Found");
        }

        Optional<PostLike> postLike=postLikeRepo.findByUserIdAndPostId(currentUserId,postId);

        if(postLike.isPresent()){
            postLikeRepo.delete(postLike.get());
            postRepo.updatePostLikes(postId,-1);
        }else{
            postLikeRepo.save(new PostLike(currentUserId,postId));
            postRepo.updatePostLikes(postId,1);
            // handling notifications later
        }
        return new LikeResponse(postLike.isEmpty());
    }


    public CommentRepresentation addPostComment(String postId, CommentCreationRequest commentRequest){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post post=postRepo.findById(postId).orElseThrow(()-> new ContentNotFoundException("Post Not Found"));

        String postOwnerId=post.getUserId();

        boolean isOwner=postOwnerId.equals(currentUserId);

        if(!isOwner){
            boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,post.getUserId());

            if(!isAllowed) throw new ActionNotAllowedException("Post Not Found");

            if(post.getPostStatus()!= Post.PostStatus.PUBLISHED) throw new ContentNotAvailableException("Post Not Found");

            PostSettings postSettings=post.getPostSettings();

            if(postSettings.isCommentsDisabled()) throw new ActionNotAllowedException("Comments Are Disabled On This Post");
        }



        Comment comment=commentRepo.save(new Comment(null,commentRequest.getContent(),currentUserId,postId,postOwnerId));

        postRepo.updatePostComments(postId,1);
        // handling notification later
        return contentmapper.toCommentRepresentation(comment);
    }

    // in this method i am dealing with both top level comments and replies on comment
    public void removePostComment(String commentId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Comment comment=commentRepo.findWithDetailsById(commentId).orElseThrow(()->new ContentNotFoundException("Comment Not Found"));
        boolean isAllowed=comment.getUserId().equals(currentUserId)||comment.getPostOwnerId().equals(currentUserId);
        if(!isAllowed) throw new UnauthorizedResourceAccessException("Comment Not Found");
        Comment parentComment=comment.getParentComment();
        String postId=comment.getPostId();
        // here i am handling the case where the comment is a reply so i will just decrement the comment count on both post and the comment
        if(parentComment!=null){
            commentRepo.updateCommentReplies(parentComment.getId(),-1);
            postRepo.updatePostComments(postId,-1);
        }else{
            int count= commentRepo.countByParentComment(comment);
            postRepo.updatePostComments(postId,-(count+1));
        }
        commentRepo.delete(comment);
    }

}
