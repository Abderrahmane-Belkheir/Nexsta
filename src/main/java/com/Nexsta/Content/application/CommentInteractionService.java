package com.Nexsta.Content.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Content.Exceptions.ContentNotFoundException;
import com.Nexsta.Content.api.dto.CommentCreationRequest;
import com.Nexsta.Content.api.dto.CommentRepresentation;
import com.Nexsta.Content.api.dto.LikeResponse;
import com.Nexsta.Content.domain.*;
import com.Nexsta.Content.domain.*;
import com.Nexsta.Content.persistence.CommentRepo;
import com.Nexsta.Content.persistence.LikeRepo;
import com.Nexsta.Content.persistence.PostRepo;
import com.Nexsta.Shared.Exceptions.ActionNotAllowedException;
import com.Nexsta.Shared.Mappers.Contentmapper;
import com.Nexsta.Shared.VisibilityPolicy;
import com.Nexsta.User.application.AuthenticatedUserService;

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
