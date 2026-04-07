package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.PostPreviewRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostPreviewRequest;
import com.example.SocialMediaApp.Content.api.dto.PostPreviewResponse;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostPreview;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import com.example.SocialMediaApp.Shared.ViewerType;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;



@Service
@RequiredArgsConstructor
public class PostPreviewQueryService {
    private final PostRepo postRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final Contentmapper contentmapper;
    private final MediaUrlResolver mediaUrlResolver;


    public PostPreviewResponse getMyPostsPreview(PostPreviewRequest request){
        String currentUserId=authenticatedUserService.getCurrentUser();
        return getPostsPreviewRepresentation(currentUserId,request.getStatus(),request.getCursor(),ViewerType.OWNER);
    }

    @CheckUserExistence
    public PostPreviewResponse getUserPostsPreview(String targetId,PostPreviewRequest request){
        String currentUserId=authenticatedUserService.getCurrentUser();

        if(currentUserId.equals(targetId)) return getMyPostsPreview(request);

        if(!visibilityPolicy.isAllowed(currentUserId,targetId)) throw new ContentNotAvailableException("This content is not available");

        return getPostsPreviewRepresentation(targetId, Post.PostStatus.PUBLISHED,request.getCursor(),ViewerType.VIEWER);
    }

    private PostPreviewResponse getPostsPreviewRepresentation(String userId, Post.PostStatus postStatus, Instant cursor, ViewerType viewerType){

        int limit=10;
        boolean hasMore=false;
        Instant nextCursor=null;

        List<Post> postList=cursor==null?
                postRepo.findTop10ByUserIdAndPostStatusOrderByPublishedAtDesc(userId,postStatus):
                postRepo.findTop10ByUserIdAndPostStatusAndPublishedAtBeforeOrderByPublishedAtDesc(userId,postStatus,cursor);


        List<PostPreviewRepresentation> postPreviewRepresentationList= postList.stream().map(post->{
            PostPreviewRepresentation postPreviewRepresentation= contentmapper.toPostPreview(post);
            if(postStatus== Post.PostStatus.PUBLISHED) {
                PostPreview postPreview=post.getPostPreview();
                String thumbnailPath=mediaUrlResolver.resolvePath(post.getPostFolderPath(),postPreview.getThumbnail());
                String thumbnailFullUrl=mediaUrlResolver.resolveFullUrl(thumbnailPath);
                postPreview.setThumbnail(thumbnailFullUrl);
                postPreviewRepresentation.setPostPreview(postPreview);
            }
            PostSettings postSettings=post.getPostSettings();
            if(viewerType==ViewerType.VIEWER){

                if(!postSettings.isHideLikes()){
                    postPreviewRepresentation.setLikes(post.getLikeCount());
                }

                if(!postSettings.isHideComments()){
                    postPreviewRepresentation.setComments(post.getCommentCount());
                }

            }else{
                // likes and comments count can be seen by the owner directly.
                postPreviewRepresentation.setLikes(post.getLikeCount());
                postPreviewRepresentation.setComments(post.getCommentCount());
            }

            return postPreviewRepresentation;
        }).toList();

        if(postPreviewRepresentationList.size()==limit){
            Post lastPost=postList.get(postList.size()-1);
            hasMore=postRepo.existsByUserIdAndPostStatusAndPublishedAtBefore(userId,postStatus,lastPost.getPublishedAt());
            if(hasMore){
                nextCursor=lastPost.getPublishedAt();
            }
        }

        return new PostPreviewResponse(postPreviewRepresentationList,hasMore,nextCursor);
    }

}
