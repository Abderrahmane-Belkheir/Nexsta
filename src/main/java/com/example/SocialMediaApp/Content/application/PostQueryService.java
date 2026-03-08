package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
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
import com.example.SocialMediaApp.User.persistence.UserRepo;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepo postRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final ProfileQueryService profileQueryService;
    private final Contentmapper contentmapper;
    private final PostLikeRepo postLikeRepo;
    private final UserRepo userRepo;
    private final MediaRepo mediaRepo;


    public Page<PostRepresentation> getMyPosts(Post.PostStatus postStatus, int page){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Pageable pageable=getPageable(page,postStatus);
        return getPostsRepresentation(currentUserId,postStatus,pageable,ViewerType.OWNER,null);
    }

    @CheckUserExistence
    public Page<PostRepresentation> getUserPosts(String targetId, int page){
        String currentUserId=authenticatedUserService.getCurrentUser();

        if(currentUserId.equals(targetId)) return getMyPosts(Post.PostStatus.PUBLISHED,page);
        if(!visibilityPolicy.isAllowed(currentUserId,targetId)) throw new ContentNotAvailableException("");

        ProfileInfo profileInfo =profileQueryService.getUserProfileInfo(targetId);
        Post.PostStatus postStatus= Post.PostStatus.PUBLISHED;
        Pageable pageable=getPageable(page,postStatus);

        Page<Post> postPage = postRepo.findByUserIdAndPostStatus(targetId, postStatus, pageable);

        List<String> postIds = postPage.get()
                .map(Post::getId)
                .toList();

        Set<String> likedPostIds = postLikeRepo.getLikesPostIds(currentUserId, postIds);


        return getPostsRepresentation(targetId, Post.PostStatus.PUBLISHED,pageable,ViewerType.VIEWER,likedPostIds::contains);
    }

    private Page<PostRepresentation> getPostsRepresentation(String userId, Post.PostStatus postStatus, Pageable pageable, ViewerType viewerType, Function<String,Boolean> likesFunction){

        Page<Post> postList=postRepo.findByUserIdAndPostStatus(userId,postStatus,pageable);
        List<String> postIds=postList.stream().map(Post::getId).toList();
        Map<String,List<MediaRepresentation>> mediaRepresentationList= mediaRepo.findByPostIdIn(postIds).stream().collect(Collectors.groupingBy(
                media -> media.getPost().getId(),
                Collectors.mapping(contentmapper::toMediaRepresentation, Collectors.toList())
        ));

        return  postList.map(post->{
            String postId=post.getId();
            List<MediaRepresentation> mediaRepresentations=mediaRepresentationList.get(postId);
            PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
            postRepresentation.setPostStatus(postStatus);
            postRepresentation.setLikes(post.getLikeCount());
            postRepresentation.setComments(post.getCommentCount());

            postRepresentation.getMediaList().addAll(mediaRepresentations);

            if(viewerType==ViewerType.VIEWER){

                PostSettings postSettings=post.getPostSettings();

                if(!postSettings.isHideLikes()){
                    postRepresentation.setLikes(post.getLikeCount());
                }

                if(postSettings.isCommentsDisabled()){
                    postRepresentation.setCommentsDisabled(true);
                }

                if(!postSettings.isHideComments()){
                    postRepresentation.setComments(post.getCommentCount());
                }
                postRepresentation.setLikedByMe(likesFunction.apply(postId));
            }

            return postRepresentation;
        });
    }

    private Pageable getPageable(int page, Post.PostStatus postStatus) {
        String sortBy = switch (postStatus) {
            case PUBLISHED -> "publishedAt";
            case DELETED -> "deletedAt";
            case DRAFT -> "createdAt";
            case UNPUBLISHED -> "unPublishedAt";
        };
        return PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, sortBy));
    }

}
