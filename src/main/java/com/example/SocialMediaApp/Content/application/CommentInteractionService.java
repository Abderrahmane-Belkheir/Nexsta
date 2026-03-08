package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.CommentRequest;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.domain.Comment;
import com.example.SocialMediaApp.Content.domain.Like;
import com.example.SocialMediaApp.Content.domain.LikeType;
import com.example.SocialMediaApp.Content.domain.Reply;
import com.example.SocialMediaApp.Content.persistence.CommentRepo;
import com.example.SocialMediaApp.Content.persistence.LikeRepo;
import com.example.SocialMediaApp.Content.persistence.ReplyRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
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
    private final ReplyRepo replyRepo;

    // toggle between Comment liked and not liked
    public LikeResponse addCommentLike(String commentId){

        String currentUserId=authenticatedUserService.getCurrentUser();

        Comment comment=commentRepo.findById(commentId).orElseThrow(()-> new ContentNotFoundException("Comment Not Found"));

        boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,comment.getPostOwnerId());

        if(!isAllowed){
            throw new ActionNotAllowedException("Action could not be completed");
        }

        boolean liked=likeRepo.existsByUserIdAndTargetIdAndLikeType(currentUserId,commentId, LikeType.COMMENT);

        long likeCount;

        if(liked){
            likeRepo.deleteByUserIdAndTargetIdAndLikeType(currentUserId,commentId,LikeType.COMMENT);
            likeCount=commentRepo.updateCommentLikes(commentId,-1);
        }else{
            likeRepo.save(new Like(currentUserId,commentId, LikeType.COMMENT));
            likeCount=commentRepo.updateCommentLikes(commentId,1);
        }

        return new LikeResponse(!liked,likeCount);
    }

    public void addCommentReply(String commentId, CommentRequest commentRequest){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Comment comment=commentRepo.findById(commentId).orElseThrow(()-> new ContentNotFoundException("Comment Not Found"));

        boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,comment.getPostOwnerId());

        if(!isAllowed){
            throw new ActionNotAllowedException("Action could not be completed");
        }

        replyRepo.save(new Reply(currentUserId,commentId,commentRequest.getContent()));
        commentRepo.updateCommentReplies(commentId,1);

    }

}
