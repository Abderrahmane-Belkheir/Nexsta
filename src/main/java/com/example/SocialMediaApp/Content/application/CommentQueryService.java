package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.CommentRepresentation;
import com.example.SocialMediaApp.Content.domain.Comment;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.CommentRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepo commentRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final PostRepo postRepo;
    private final Contentmapper contentmapper;
    private final static int pageSize=20;

    private Page<CommentRepresentation> getCommentsRepresentations(Supplier<Page<Comment>> commentPageSupplier, Supplier<Post> postSupplier){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post targetPost=postSupplier.get();
        enforceCommentVisibility(targetPost,currentUserId);
        Page<Comment> commentPage=commentPageSupplier.get();
        return commentPage.map(contentmapper::toCommentRepresentation);
    }

    // comments are sorted from oldest to newest whereas the replies are with the inverse order
    public Page<CommentRepresentation> getPostComments(String postId,int page){
        return getCommentsRepresentations(()->commentRepo.findByPostIdAndParentComment(postId,null, PageRequest.of(page,pageSize, Sort.by(Sort.Direction.ASC,"createdAt"))),()->postRepo.findById(postId).orElseThrow(()->new ContentNotFoundException("Post Not Found")));
    }

    public Page<CommentRepresentation> getCommentReplies(String commentId,int page){
        Comment comment=commentRepo.findWithDetailsById(commentId).orElseThrow(()->new ContentNotFoundException("Comment Not Found"));
        if(comment.getParentComment()!=null) throw new ContentNotAvailableException("Cannot get replies of a reply");
        return getCommentsRepresentations(()->commentRepo.findByParentComment(comment,PageRequest.of(page,pageSize,Sort.by(Sort.Direction.DESC,"createdAt"))), comment::getPost);
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
