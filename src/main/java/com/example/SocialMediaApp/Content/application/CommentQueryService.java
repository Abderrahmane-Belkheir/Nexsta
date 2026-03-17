package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.CommentRepresentation;
import com.example.SocialMediaApp.Content.domain.Comment;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.CommentRepo;
import com.example.SocialMediaApp.Content.persistence.LikeRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Profile.api.dto.ProfileSummary;
import com.example.SocialMediaApp.Profile.application.ProfileSummaryBuilder;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepo commentRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileSummaryBuilder profileSummaryBuilder;
    private final VisibilityPolicy visibilityPolicy;
    private final PostRepo postRepo;
    private final LikeRepo likeRepo;
    private final Contentmapper contentmapper;
    private final static int pageSize=20;

    private Page<CommentRepresentation> getCommentsRepresentations(Supplier<Post> postSupplier,Supplier<Page<Comment>> commentPageSupplier){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post targetPost=postSupplier.get();
        enforceCommentVisibility(targetPost,currentUserId);
        Page<Comment> commentPage=commentPageSupplier.get();
        List<Comment> commentList=commentPage.getContent();
        List<String> commentIds=commentList.stream().map(Comment::getId).toList();
        Set<String> likedCommentIds= likeRepo.getLikesCommentsIds(currentUserId,commentIds);
        List<String> commenterIds = commentList.stream().map(Comment::getUserId).distinct().toList();
        Map<String, ProfileSummary> commentersMap = profileSummaryBuilder.buildProfileSummaries(commenterIds).stream()
                .collect(Collectors.toMap(ProfileSummary::getUserId, Function.identity()));
        return commentPage.map(comment -> {

            CommentRepresentation commentRepresentation=contentmapper.toCommentRepresentation(comment);

            String commentId=comment.getId();
            commentRepresentation.setLikedByMe(likedCommentIds.contains(commentId));

            String commenterId=comment.getUserId();
            ProfileSummary profileSummary = commentersMap.get(commenterId);
            commentRepresentation.setProfileSummary(profileSummary);

            return commentRepresentation;
        });
    }

    // comments are sorted from oldest to newest whereas the replies are with the inverse order
    public Page<CommentRepresentation> getPostComments(String postId,int page){
        Supplier<Post> postSupplier=()->postRepo.findById(postId).orElseThrow(()->new ContentNotFoundException("Post Not Found"));
        Supplier<Page<Comment>> commentPageSupplier=()->commentRepo.findByPostIdAndParentComment(postId,null, PageRequest.of(page,pageSize, Sort.by(Sort.Direction.ASC,"createdAt")));
        return getCommentsRepresentations(postSupplier,commentPageSupplier);
    }

    public Page<CommentRepresentation> getCommentReplies(String commentId,int page){
        Comment comment=commentRepo.findWithDetailsById(commentId).orElseThrow(()->new ContentNotFoundException("Comment Not Found"));
        if(comment.getParentComment()!=null) throw new ActionNotAllowedException("Cannot get replies of a reply");
        Supplier<Post> postSupplier=comment::getPost;
        Supplier<Page<Comment>> commentPageSupplier=()->commentRepo.findByParentComment(comment,PageRequest.of(page,pageSize,Sort.by(Sort.Direction.DESC,"createdAt")));
        return getCommentsRepresentations(postSupplier,commentPageSupplier);
    }

    private void enforceCommentVisibility(Post post, String viewerId){

        String postOwnerId=post.getUserId();

        if(postOwnerId.equals(viewerId)) return;

        boolean isAllowed=visibilityPolicy.isAllowed(viewerId,postOwnerId);
        if(!isAllowed) throw new ContentNotAvailableException("This content is not available");
        PostSettings postSettings=post.getPostSettings();
        if(postSettings.isHideComments()) throw new ContentNotAvailableException("Comments Are Hidden On This Post");

    }



}
