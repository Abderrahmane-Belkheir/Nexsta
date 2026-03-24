package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Content.persistence.MediaRepo;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepo postRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final ProfileQueryService profileQueryService;
    private final Contentmapper contentmapper;
    private final PostLikeRepo postLikeRepo;
    private final MediaRepo mediaRepo;
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

    private Page<PostRepresentation> getPostsRepresentation(String userId, Post.PostStatus postStatus, Pageable pageable, ViewerType viewerType, Function<String,Boolean> likesFunction,ProfileInfo profileInfo){

        Page<Post> postList=postRepo.findByUserIdAndPostStatus(userId,postStatus,pageable);
        List<String> postIds=postList.stream().map(Post::getId).toList();
        Map<String,List<MediaRepresentation>> mediaRepresentationMap= mediaRepo.findByPostIdIn(postIds).stream().collect(Collectors.groupingBy(
                Media::getPostId,
                Collectors.mapping(contentmapper::toMediaRepresentation,Collectors.toList())
        ));

        return  postList.map(post->{
            String postId=post.getId();
            List<MediaRepresentation> mediaRepresentations=mediaRepresentationMap.get(postId);
            PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
            postRepresentation.setPostStatus(postStatus);
            postRepresentation.setProfileInfo(profileInfo);
            PostSettings postSettings=post.getPostSettings();
            if(viewerType==ViewerType.VIEWER){

                if(!postSettings.isHideLikes()){
                    postRepresentation.setLikes(post.getLikeCount());
                }

                if(!postSettings.isHideComments()){
                    postRepresentation.setComments(post.getCommentCount());
                }
                postRepresentation.setLikedByMe(likesFunction.apply(postId));
            }else{
                // likes and comments count can be seen by the owner directly.
                postRepresentation.setLikes(post.getLikeCount());
                postRepresentation.setComments(post.getCommentCount());
                // restored should be seen by the owner.
                postRepresentation.setRestored(post.isRestored());

                if(postStatus== Post.PostStatus.DELETED){
                    postRepresentation.setPreDeletionPostStatus(post.getPreDeletionStatus());
                }else {
                    // here the intent is clear we want to restrict the media access on deleted posts that's the cost of deletion
                    postRepresentation.getMediaList().addAll(mediaRepresentations);
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
            case DRAFT -> "createdAt";
            case UNPUBLISHED -> "unPublishedAt";
            case SCHEDULED -> "";
        };
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
    }

}
