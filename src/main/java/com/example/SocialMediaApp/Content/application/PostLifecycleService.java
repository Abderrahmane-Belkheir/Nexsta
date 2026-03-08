package com.example.SocialMediaApp.Content.application;
import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Upload.domain.MediaUpload;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PostLifecycleService {

    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final MediaLifecycleService mediaLifecycleService;
    private final Contentmapper contentmapper;

    public PostRepresentation createPost(PostCreationRequest postCreation){
        String currentUserId=authenticatedUserService.getCurrentUser();
        List<String> uploadRequestsIds=postCreation.getUploadRequestsIds();
        List<MediaUpload> mediaUploads=mediaLifecycleService.extractMediaUploads(currentUserId,uploadRequestsIds,UploadType.POST);
        Post post= postRepo.save(Post.builder().user(new User(currentUserId))
                .caption(postCreation.getCaption())
                .postSettings(postCreation.getPostSettings()).location(postCreation.getLocation()).build());
        List<Media> mediaList=mediaLifecycleService.persistMedia(mediaUploads,post);
        PostRepresentation postRepresentation=contentmapper.toPostRepresentation(post);
        postRepresentation.setPostStatus(Post.PostStatus.DRAFT);
        List<MediaRepresentation> mediaRepresentationList= mediaList.stream().map(contentmapper::toMediaRepresentation).toList();
        postRepresentation.getMediaList().addAll(mediaRepresentationList);
        return postRepresentation;
    }

    // publishing post for first time draft -> published
    public void publishPost(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post draftPost=postRepo.findByUserIdAndPostIdAndPostStatus(currentUserId,postId, Post.PostStatus.DRAFT).
                orElseThrow(()-> new ActionNotAllowedException("Action could not be completed"));
        draftPost.setPublishedAt(Instant.now());
        draftPost.setPostStatus(Post.PostStatus.PUBLISHED);
        postRepo.save(draftPost);
    }

    // switching between published <-> unpublished
    public void togglePostVisibility(String postId,Post.PostStatus status){
       List<Post.PostStatus> allowedStatus=List.of(Post.PostStatus.PUBLISHED, Post.PostStatus.UNPUBLISHED);
        if(!allowedStatus.contains(status)){
            throw new ActionNotAllowedException("Action could not be completed");
        }
        String currentUserId=authenticatedUserService.getCurrentUser();
        int updated= postRepo.updatePostStatus(postId,status,currentUserId,allowedStatus);
        if(updated==0){
            // can be thrown if post not found or user don't have access or post status is originally in draft or deleted
            throw new ActionNotAllowedException("Action could not be completed");
        }
    }


    public void deletePost(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        int updated=postRepo.updatePostStatus(postId, Post.PostStatus.DELETED,currentUserId,List.of(Post.PostStatus.values()));
        if(updated==0){
            throw new ActionNotAllowedException("Action could not be completed");
        }
    }




}
