package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostPreviewRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.PostLikeRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Profile.application.ProfileQueryService;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.ViewerType;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PostPreviewQueryService {
    private final PostRepo postRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final Contentmapper contentmapper;
    private final static int pageSize=6;


    public Page<PostPreviewRepresentation> getMyPostsPreview(Post.PostStatus postStatus, int page){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Pageable pageable=getPageable(page,pageSize,postStatus);
        return getPostsPreviewRepresentation(currentUserId,postStatus,pageable, ViewerType.OWNER);
    }

    @CheckUserExistence
    public Page<PostPreviewRepresentation> getUserPostsPreview(String targetId, int page){
        String currentUserId=authenticatedUserService.getCurrentUser();

        if(currentUserId.equals(targetId)) return getMyPostsPreview(Post.PostStatus.PUBLISHED,page);
        if(!visibilityPolicy.isAllowed(currentUserId,targetId)) throw new ContentNotAvailableException("This content is not available");

        Post.PostStatus postStatus= Post.PostStatus.PUBLISHED;
        Pageable pageable=getPageable(page,pageSize,postStatus);

        return getPostsPreviewRepresentation(targetId, Post.PostStatus.PUBLISHED,pageable,ViewerType.VIEWER);
    }

    private Page<PostPreviewRepresentation> getPostsPreviewRepresentation(String userId, Post.PostStatus postStatus, Pageable pageable, ViewerType viewerType){

        Page<Post> postList=postRepo.findByUserIdAndPostStatus(userId,postStatus,pageable);
        return  postList.map(post->{
            PostPreviewRepresentation postPreviewRepresentation= contentmapper.toPostPreview(post);
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
        });
    }


    private Pageable getPageable(int page,int size, Post.PostStatus postStatus) {
        String sortBy = switch (postStatus) {
            case PUBLISHED -> "publishedAt";
            case DELETED -> "deletedAt";
            case DRAFT,SCHEDULED -> "createdAt";
            case UNPUBLISHED -> "unPublishedAt";
        };
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
    }
}
