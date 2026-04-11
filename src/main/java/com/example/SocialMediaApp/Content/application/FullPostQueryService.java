package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentationResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.function.Supplier;


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

    public PostRepresentationResponse getPostNeighbors(String postId, FetchDirection direction){
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post post=postRepo.findById(postId).orElseThrow(()->new ContentNotAvailableException("Post Not Found"));
        String ownerId=post.getUserId();
        boolean isOwner=currentUserId.equals(ownerId);
        if(!isOwner){
            boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,post.getUserId());

            if(!isAllowed) throw new ActionNotAllowedException("Post Not Found");

            if(post.getPostStatus()!= Post.PostStatus.PUBLISHED) throw new ContentNotAvailableException("Post Not Found");
        }
        ProfileInfo profileInfo=isOwner?null:profileQueryService.getUserProfileInfo(ownerId);
        ViewerType viewerType=isOwner?ViewerType.OWNER:ViewerType.VIEWER;
        return getPostNeighborsHelper(currentUserId,ownerId,()->post,post.getPostStatus(),viewerType,profileInfo,direction);
    }

    private PostRepresentationResponse getPostNeighborsHelper(String userId, String ownerId, Supplier<Post> postSupplier, Post.PostStatus postStatus, ViewerType viewerType, ProfileInfo profileInfo, FetchDirection direction){
        Post post=postSupplier.get();
        int pageSize=5;
        Pageable pageable= PageRequest.of(0,pageSize);
        int middlePostIndex=0;
        List<Post> postList=switch (direction){
            case UP -> postRepo.findPostsAboveOrBelowPost(ownerId,post.getPublishedAt(),postStatus,FetchDirection.UP.name(),pageable) ;
            case DOWN ->  postRepo.findPostsAboveOrBelowPost(ownerId,post.getPublishedAt(),postStatus,FetchDirection.DOWN.name(),pageable);
            case MIXED -> {
                List<Post> previousPosts=postRepo.findPostsAboveOrBelowPost(ownerId,post.getPublishedAt(),postStatus,FetchDirection.DOWN.name(),pageable);
                List<Post> nextPosts=postRepo.findPostsAboveOrBelowPost(ownerId,post.getPublishedAt(),postStatus,FetchDirection.UP.name(),pageable);
                previousPosts.add(post);
                middlePostIndex=previousPosts.size()-1;
                previousPosts.addAll(nextPosts);
              yield  previousPosts;
            }
        };

        if(postList.isEmpty()) return  new PostRepresentationResponse();
        Map<String,List<MediaRepresentation>> mediaRepresentationMap=mediaLifecycleService.getPostsMedia(postList,postStatus);
        Set<String> likeByPost= postLikeRepo.getLikesPostIds(userId,postList.stream().map(Post::getId).toList());
        List<PostRepresentation> postRepresentationList=postList.stream().map(p ->{
            String postId= p.getId();
            List<MediaRepresentation> mediaRepresentations=mediaRepresentationMap.getOrDefault(postId,new ArrayList<>());
            PostRepresentation postRepresentation=contentmapper.toPostRepresentation(p);
            postRepresentation.setPostStatus(postStatus);
            postRepresentation.setProfileInfo(profileInfo);
            postRepresentation.getMediaList().addAll(mediaRepresentations);
            PostSettings postSettings= p.getPostSettings();
            if(viewerType==ViewerType.VIEWER){

                if(!postSettings.isHideLikes()){
                    postRepresentation.setLikes(p.getLikeCount());
                }

                if(!postSettings.isHideComments()){
                    postRepresentation.setComments(p.getCommentCount());
                }

            }else{
                // likes and comments count can be seen by the owner directly.
                postRepresentation.setLikes(p.getLikeCount());
                postRepresentation.setComments(p.getCommentCount());
                // restored should be seen by the owner.
                postRepresentation.setRestored(p.isRestored());

                postRepresentation.setCreatedAt(p.getCreatedAt());
                if(postStatus== Post.PostStatus.DELETED){
                    postRepresentation.setPreDeletionPostStatus(p.getPreDeletionStatus());
                }

            }
            postRepresentation.setLikedByMe(likeByPost.contains(p.getId()));
            postRepresentation.setCommentsDisabled(postSettings.isCommentsDisabled());

            return postRepresentation;
        }).toList();

        if(direction==FetchDirection.MIXED){
            List<PostRepresentation> previousPostRepresentations=postRepresentationList.subList(0,middlePostIndex);
            PostRepresentation middlePostRepresentation=postRepresentationList.get(middlePostIndex);
            List<PostRepresentation> nextPostRepresentations=postRepresentationList.subList(middlePostIndex+1,postList.size());
            return new PostRepresentationResponse(previousPostRepresentations,middlePostRepresentation,nextPostRepresentations);
        }

        return new PostRepresentationResponse(postRepresentationList,direction);
    }


    }

