package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.PostUpdateRequest;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.UploadFinalization;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostUpdateService {

    private final PostRepo postRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final MediaLifecycleService mediaLifecycleService;
    private final StorageService storageService;
    public void updatePost(PostUpdateRequest request){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserIdWithMediaList(currentUserId,request.getId()).
                orElseThrow(()->new ContentNotAvailableException("Post to Update Not Found"));
        post.setCaption(request.getCaption());
        post.setPostSettings(request.getPostSettings());
        if(post.getPostStatus()!= Post.PostStatus.PUBLISHED&&post.getPostStatus()!= Post.PostStatus.UNPUBLISHED){
            // only never published posts are allowed to update media
            List<Media> mediaList=post.getMediaList();
            List<String> mediaIds=request.getMediaIds();
            mediaIds.removeAll(mediaList.stream().map(Media::getId).toList());
            UploadFinalization uploadFinalization=mediaLifecycleService.extractMediaUploads(currentUserId,mediaIds, UploadType.POST);
            mediaLifecycleService.persistMedia(uploadFinalization.getMediaUploads(),post);
            storageService.transferTemporaryContent(post.getPostFolderPath(),uploadFinalization.getFilePaths());
        }
        postRepo.save(post);
    }

}
