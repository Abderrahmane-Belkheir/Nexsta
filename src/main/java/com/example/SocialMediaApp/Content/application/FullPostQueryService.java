package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.domain.FetchDirection;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.PostLikeRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Profile.application.ProfileQueryService;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.ViewerType;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class FullPostQueryService {

    private final PostRepo postRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final ProfileQueryService profileQueryService;
    private final Contentmapper contentmapper;
    private final PostLikeRepo postLikeRepo;
    private final MediaLifecycleService mediaLifecycleService;
    private final static int pageSize=6;


    public Page<PostRepresentation> getMyPosts(Post.PostStatus postStatus, int page){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Pageable pageable=getPageable(page,pageSize,postStatus);
        return getPostsRepresentation(currentUserId,postStatus,pageable,ViewerType.OWNER,null,null);
    }

    @CheckUserExistence
    public Page<PostRepresentation> getUserPosts(String targetId, int page){
        String currentUserId=authenticatedUserService.getCurrentUser();

        if(currentUserId.equals(targetId)) return getMyPosts(Post.PostStatus.PUBLISHED,page);
        if(!visibilityPolicy.isAllowed(currentUserId,targetId)) throw new ContentNotAvailableException("This content is not available");

        ProfileInfo profileInfo =profileQueryService.getUserProfileInfo(targetId);
        Post.PostStatus postStatus= Post.PostStatus.PUBLISHED;
        Pageable pageable=getPageable(page,pageSize,postStatus);

        Page<Post> postPage = postRepo.findByUserIdAndPostStatus(targetId, postStatus, pageable);

        List<String> postIds = postPage.get()
                .map(Post::getId)
                .toList();

        Set<String> likedPostIds = postLikeRepo.getLikesPostIds(currentUserId, postIds);


        return getPostsRepresentation(targetId, Post.PostStatus.PUBLISHED,pageable,ViewerType.VIEWER,likedPostIds::contains,profileInfo);
    }



    public Page<PostRepresentation> getPostNeighbors(String postId, FetchDirection direction){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post post=postRepo.findById(postId).orElseThrow(()->new ContentNotAvailableException("Post Not Found"));

        boolean isOwner=currentUserId.equals(post.getUserId());
        if(!isOwner){
            boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,post.getUserId());

            if(!isAllowed) throw new ActionNotAllowedException("Post Not Found");

            if(post.getPostStatus()!= Post.PostStatus.PUBLISHED) throw new ContentNotAvailableException("Post Not Found");
        }

        return null;
    }

    private Page<PostRepresentation> getPostNeighborsHelper(String postId, Post.PostStatus postStatus, ViewerType viewerType,ProfileInfo profileInfo){

        Page<Post> postList=null;
                //postRepo.findByUserIdAndPostStatus(userId,postStatus,pageable);
        Map<String,List<MediaRepresentation>> mediaRepresentationMap=mediaLifecycleService.getPostsMedia(postList.getContent(),postStatus);

        return  postList.map(post->{
            String postId=post.getId();
            List<MediaRepresentation> mediaRepresentations=mediaRepresentationMap.getOrDefault(postId,new ArrayList<>());
            PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
            postRepresentation.setPostStatus(postStatus);
            postRepresentation.setProfileInfo(profileInfo);
            postRepresentation.getMediaList().addAll(mediaRepresentations);
            PostSettings postSettings=post.getPostSettings();
            if(viewerType==ViewerType.VIEWER){

                if(!postSettings.isHideLikes()){
                    postRepresentation.setLikes(post.getLikeCount());
                }

                if(!postSettings.isHideComments()){
                    postRepresentation.setComments(post.getCommentCount());
                }

                postRepresentation.setLikedByMe(null);

            }else{
                // likes and comments count can be seen by the owner directly.
                postRepresentation.setLikes(post.getLikeCount());
                postRepresentation.setComments(post.getCommentCount());
                // restored should be seen by the owner.
                postRepresentation.setRestored(post.isRestored());

                postRepresentation.setCreatedAt(post.getCreatedAt());
                if(postStatus== Post.PostStatus.DELETED){
                    postRepresentation.setPreDeletionPostStatus(post.getPreDeletionStatus());
                }

            }

                postRepresentation.setCommentsDisabled(postSettings.isCommentsDisabled());

            return postRepresentation;
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
