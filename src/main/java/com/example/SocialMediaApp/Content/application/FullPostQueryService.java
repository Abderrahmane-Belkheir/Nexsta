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
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.ViewerType;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.*;


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

    public List<PostRepresentation> getPostNeighbors(String postId, FetchDirection direction){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post post=postRepo.findById(postId).orElseThrow(()->new ContentNotAvailableException("Post Not Found"));

        boolean isOwner=currentUserId.equals(post.getUserId());
        if(!isOwner){
            boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,post.getUserId());

            if(!isAllowed) throw new ActionNotAllowedException("Post Not Found");

            if(post.getPostStatus()!= Post.PostStatus.PUBLISHED) throw new ContentNotAvailableException("Post Not Found");
        }
        ProfileInfo profileInfo=profileQueryService.getUserProfileInfo(currentUserId);
        ViewerType viewerType=isOwner?ViewerType.OWNER:ViewerType.VIEWER;
        return getPostNeighborsHelper(currentUserId,postId,post.getPostStatus(),viewerType,profileInfo,direction);
    }

    private List<PostRepresentation> getPostNeighborsHelper(String userId,String postId, Post.PostStatus postStatus, ViewerType viewerType,ProfileInfo profileInfo,FetchDirection direction){

        List<Post> postList=switch (direction){
            case UP ->  ;
            case DOWN ->  ;
            case MIXED -> ;
        };

        if(postList.isEmpty()) return new ArrayList<>();
        Map<String,List<MediaRepresentation>> mediaRepresentationMap=mediaLifecycleService.getPostsMedia(postList,postStatus);
        Set<String> likeByPost= postLikeRepo.getLikesPostIds(userId,postList.stream().map(Post::getId).toList());
        return  postList.stream().map(post->{
        //    String postId=post.getId();
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
            postRepresentation.setLikedByMe(likeByPost.contains(post.getId()));
            postRepresentation.setCommentsDisabled(postSettings.isCommentsDisabled());

            return postRepresentation;
        }).toList();
    }

    }

}
