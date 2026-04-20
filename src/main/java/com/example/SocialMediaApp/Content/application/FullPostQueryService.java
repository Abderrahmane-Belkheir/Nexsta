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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

import static com.example.SocialMediaApp.Content.domain.FetchDirection.*;


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

    @Transactional(readOnly = true)
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


    /*
    TODO: publishedAt/createdAt is not guaranteed unique across posts.
      Timestamp collision will cause skipped or duplicated posts in pagination.
     */

    private PostRepresentationResponse getPostNeighborsHelper(String userId, String ownerId, Supplier<Post> postSupplier, Post.PostStatus postStatus, ViewerType viewerType, ProfileInfo profileInfo, FetchDirection direction){
        Post post=postSupplier.get();

        PostFetch postFetch =fetchPosts(ownerId,post,direction);
        List<Post> postList=postFetch.getPosts();
        int middlePostIndex=postFetch.getMiddlePostIndex();


        postList=new ArrayList<>(postList);

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

        if(direction== MIXED){
            List<PostRepresentation> previousPostRepresentations=postRepresentationList.subList(0,middlePostIndex);
            PostRepresentation middlePostRepresentation=postRepresentationList.get(middlePostIndex);
            List<PostRepresentation> nextPostRepresentations=postRepresentationList.subList(middlePostIndex+1,postList.size());
            return new PostRepresentationResponse(previousPostRepresentations,middlePostRepresentation,nextPostRepresentations);
        }

        return new PostRepresentationResponse(postRepresentationList,direction);
    }

    private PostFetch fetchPosts(String userId, Post post, FetchDirection direction){

        int pageSize=5;
        Pageable pageable= PageRequest.of(0,pageSize);
        Post.PostStatus postStatus=post.getPostStatus();
        Instant middlePostTimeStamp=deducePostTimeStamp(post);

        return switch (direction){

            case UP,DOWN -> {

                List<Post> p=new ArrayList<>(postStatus==
                        Post.PostStatus.PUBLISHED||postStatus== Post.PostStatus.UNPUBLISHED?postRepo.findPostsAboveOrBelowPostOrderByPublishedAt(userId,middlePostTimeStamp,postStatus, direction.name(),pageable)
                        :postRepo.findPostsAboveOrBelowPostOrderByCreatedAt(userId,middlePostTimeStamp,postStatus,direction.name(),pageable));

                if(direction==UP) Collections.reverse(p);

              yield   new PostFetch(p,0);

            }

            case MIXED -> {

                List<Post> mixedPostNeighbors=new ArrayList<>(postRepo.findMixedNeighbors(userId,middlePostTimeStamp,postStatus.name(),pageSize));

                // No neighbors at all — middle post stands alone
                if(mixedPostNeighbors.isEmpty()){
                    mixedPostNeighbors.add(post);
                    yield new PostFetch(mixedPostNeighbors,0);
                }

                // All neighbors are above — middle post is the earliest, insert at front
                Post firstNeighbor=mixedPostNeighbors.get(0);
                Instant firstNeighborTimestamp=deducePostTimeStamp(firstNeighbor);
                if(middlePostTimeStamp.isBefore(firstNeighborTimestamp)){
                    mixedPostNeighbors.add(0,post);
                    yield new PostFetch(mixedPostNeighbors,0);
                }

                // Find the boundary where below neighbors end and above neighbors begin
                for(int i=1;i<mixedPostNeighbors.size();i++){
                    Post neighbor=mixedPostNeighbors.get(i);
                    Instant neighborTimestamp=deducePostTimeStamp(neighbor);
                    if(neighborTimestamp.isAfter(middlePostTimeStamp)){
                        mixedPostNeighbors.add(i,post);
                        yield new PostFetch(mixedPostNeighbors,i);
                    }
                }

                // All neighbors are below — middle post is the latest, append at end
                mixedPostNeighbors.add(post);
                yield new PostFetch(mixedPostNeighbors,mixedPostNeighbors.size()-1);

            }
        };
    }

    private Instant deducePostTimeStamp(Post post){
        return post.getPostStatus() == Post.PostStatus.PUBLISHED || post.getPostStatus()== Post.PostStatus.UNPUBLISHED?post.getPublishedAt():post.getCreatedAt();
    }

    @Getter
    @AllArgsConstructor
   private static class PostFetch{
        private List<Post> posts;
        private int middlePostIndex;
    }

    }

