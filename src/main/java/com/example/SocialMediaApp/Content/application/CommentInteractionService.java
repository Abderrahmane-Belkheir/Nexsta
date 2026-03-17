package com.example.SocialMediaApp.Content.application;

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

@Service
@RequiredArgsConstructor
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

        Comment comment=commentRepo.findById(commentId).orElseThrow(()-> new ContentNotFoundException("Comment Not Found"));

        boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,comment.getPostOwnerId());

        if(!isAllowed){
            throw new ActionNotAllowedException("Action could not be completed");
        }

        boolean liked=likeRepo.existsByUserIdAndTargetIdAndType(currentUserId,commentId, LikeType.COMMENT);

        if(liked){
            likeRepo.deleteByUserIdAndTargetIdAndType(currentUserId,commentId,LikeType.COMMENT);
            commentRepo.updateCommentLikes(commentId,-1);
        }else{
            likeRepo.save(new Like(currentUserId,commentId, LikeType.COMMENT));
            commentRepo.updateCommentLikes(commentId,1);
        }

        return new LikeResponse(!liked);
    }

    public CommentRepresentation addCommentReply(String commentId, CommentCreationRequest commentRequest){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Comment comment=commentRepo.findWithDetailsById(commentId).orElseThrow(()-> new ContentNotFoundException("Comment Not Found"));

        boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,comment.getPostOwnerId());

        if(!isAllowed) throw new ActionNotAllowedException("Action could not be completed");

        Post post=comment.getPost();
        PostSettings postSettings=post.getPostSettings();

        if(postSettings.isCommentsDisabled()) throw new ActionNotAllowedException("Comments Are Disabled On This Post");

        if(comment.getParentComment()!=null) throw new ActionNotAllowedException("Cannot Reply on a Reply");

        commentRepo.save(new Comment(comment,commentRequest.getContent(),currentUserId,post.getId(),comment.getPostOwnerId()));
        commentRepo.updateCommentReplies(commentId,1);
        postRepo.updatePostComments(post.getId(),1);
        CommentRepresentation commentRepresentation=contentmapper.toCommentRepresentation(comment);
        commentRepresentation.setReplyCount(null);
        return commentRepresentation;
    }

}
