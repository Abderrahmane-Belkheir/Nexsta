package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.CommentCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.CommentRepresentation;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.domain.*;
import com.example.SocialMediaApp.Content.persistence.CommentRepo;
import com.example.SocialMediaApp.Content.persistence.LikeRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentInteractionService {

    private final CommentRepo commentRepo;
    private final VisibilityPolicy visibilityPolicy;
    private final AuthenticatedUserService authenticatedUserService;
    private final LikeRepo likeRepo;
    private final PostRepo postRepo;
    private final Contentmapper contentmapper;

    // toggle between Comment liked and not liked
    public LikeResponse addCommentLike(String commentId){

        String currentUserId=authenticatedUserService.getCurrentUser();

        Comment comment=commentRepo.findWithDetailsById(commentId).orElseThrow(()-> new ContentNotFoundException("Comment Not Found"));

        String postOwnerId=comment.getPostOwnerId();

        boolean isOwner=comment.getPostOwnerId().equals(currentUserId);

        if(!isOwner){
            boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,postOwnerId);

            if(!isAllowed) throw new ActionNotAllowedException("Post Not Found");
            Post post=comment.getPost();
            if(post.getPostStatus()!= Post.PostStatus.PUBLISHED) throw new ContentNotAvailableException("Post Not Found");
        }

        Optional<Like> like=likeRepo.findByUserIdAndCommentId(currentUserId,commentId);

        if(like.isPresent()){
            likeRepo.delete(like.get());
            commentRepo.updateCommentLikes(commentId,-1);
        }else{
            likeRepo.save(new Like(currentUserId,commentId, LikeType.COMMENT));
            commentRepo.updateCommentLikes(commentId,1);
        }

        return new LikeResponse(like.isEmpty());
    }

    public CommentRepresentation addCommentReply(String commentId, CommentCreationRequest commentRequest){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Comment parentComment =commentRepo.findWithDetailsById(commentId).orElseThrow(()-> new ContentNotFoundException("Comment Not Found"));

        String postOwnerId= parentComment.getPostOwnerId();

        boolean isOwner=postOwnerId.equals(currentUserId);

        if(!isOwner){
            boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,postOwnerId);

            if(!isAllowed) throw new ActionNotAllowedException("Post Not Found");
            Post post= parentComment.getPost();
            if(post.getPostStatus()!= Post.PostStatus.PUBLISHED) throw new ContentNotAvailableException("Post Not Found");
            PostSettings postSettings=post.getPostSettings();
            if(postSettings.isCommentsDisabled()) throw new ActionNotAllowedException("Comments Are Disabled On This Post");
        }

        Post post= parentComment.getPost();

        if(parentComment.getParentComment()!=null) throw new ActionNotAllowedException("Cannot Reply on a Reply");

        Comment comment=commentRepo.save(new Comment(parentComment,commentRequest.getContent(),currentUserId,post.getId(), parentComment.getPostOwnerId()));
        commentRepo.updateCommentReplies(commentId,1);
        postRepo.updatePostComments(post.getId(),1);
        CommentRepresentation commentRepresentation=contentmapper.toCommentRepresentation(comment);
        commentRepresentation.setReplyCount(null);
        return commentRepresentation;
    }

}
