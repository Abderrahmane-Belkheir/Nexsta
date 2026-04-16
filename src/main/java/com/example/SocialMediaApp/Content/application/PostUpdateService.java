package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostUpdateRequest;
import com.example.SocialMediaApp.Content.api.dto.PostUpdateResponse;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostPreview;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.StorageTransferManager;
import com.example.SocialMediaApp.Upload.domain.MediaUpload;
import com.example.SocialMediaApp.Upload.domain.UploadFinalization;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostUpdateService {

    private final PostRepo postRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final MediaLifecycleService mediaLifecycleService;
    private final StorageService storageService;
    private final StorageTransferManager storageTransferManager;
    private final ThumbnailService thumbnailService;
    private final Contentmapper contentmapper;
    private final MediaUrlResolver mediaUrlResolver;


    public void updatePost(PostUpdateRequest request){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserIdWithMediaList(currentUserId,request.getId()).
                orElseThrow(()->new ContentNotAvailableException("Post to Update Not Found"));

        post.setCaption(request.getCaption());
        post.setPostSettings(request.getPostSettings());

        // prepare those if media changing is needed
        UploadFinalization uploadFinalization=null;
        Map<String,Media> existingMediaMap=new HashMap<>();
        String thumbnailPath=null;

        if(post.getPostStatus()!= Post.PostStatus.PUBLISHED&&post.getPostStatus()!= Post.PostStatus.UNPUBLISHED){
            // only never published posts are allowed to update media
            List<Media> existingMediaList =post.getMediaList();
            List<String> newMediaIds =request.getMediaIds();
            existingMediaMap = existingMediaList.stream()
                    .collect(Collectors.toMap(Media::getId, m -> m));
            existingMediaList.clear();
            Map<String, Media> finalExistingMediaMap = existingMediaMap;
            List<String> newIds =newMediaIds.stream().filter(id-> finalExistingMediaMap.get(id)==null).toList();
            if(!newIds.isEmpty()){
                uploadFinalization=mediaLifecycleService.extractMediaUploads(currentUserId, newIds, UploadType.POST);
                for(MediaUpload mediaUpload:uploadFinalization.getMediaUploads()){
                    String mediaId=mediaUpload.getId();
                    Media media=Media.builder().id(mediaId).mediaType(mediaUpload.getMediaType()).
                            post(post).build();
                    existingMediaMap.put(mediaId,media);
                }
            }
            for(int i=0;i<request.getMediaIds().size();i++){
                String id=request.getMediaIds().get(i);
                Media media=existingMediaMap.get(id);
                if (media!=null){
                    media.setDisplayOrder(i);
                    existingMediaList.add(media);
                    existingMediaMap.remove(id);
                }
            }


            PostPreview existsingPostPreview =post.getPostPreview();
            String existingThumbnail=existsingPostPreview.getThumbnail();
            String thumbnail=request.getThumbnail();
            if(thumbnail!=null&& !thumbnail.equals(existingThumbnail)){
                existingMediaMap.put(existingThumbnail,new Media());
                PostPreview newPostPreview=thumbnailService.generatePostThumbnail(currentUserId,thumbnail, existingMediaList.get(0));
                existsingPostPreview.setThumbnail(newPostPreview.getThumbnail());
                existsingPostPreview.setMediaType(newPostPreview.getMediaType());
                thumbnailPath=newPostPreview.getThumbnailFilePath();
            }

        }

        postRepo.save(post);

        if(!existingMediaMap.isEmpty()){
            List<String> deletedMediaPaths =existingMediaMap.keySet().stream().
                    map(s -> mediaUrlResolver.resolvePath(post.getPostFolderPath(),s)).toList();
        storageService.deleteFiles(deletedMediaPaths,storageTransferManager.resolveBucket(post.getPostStatus()));
        }

        if(uploadFinalization!=null){
            Post.PostStatus status=post.getPostStatus();
            StorageTransferManager.StorageTransfer storageTransfer=storageTransferManager.resolveStorageTransfer(status,status);
            uploadFinalization.getFilePaths().add(thumbnailPath);
            storageService.transferTemporaryContent(post.getPostFolderPath(),uploadFinalization.getFilePaths(),storageTransfer);
        }

    }

    public PostUpdateResponse getPostToUpdate(String postId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Post post=postRepo.findByIdAndUserId(postId,currentUserId).
                orElseThrow(()->new ContentNotAvailableException("Post Not Found"));
        Map<String,List<MediaRepresentation>> mediaRepresentationMap=mediaLifecycleService.getPostsMedia(List.of(post),post.getPostStatus());
       List<MediaRepresentation>  mediaRepresentationList=mediaRepresentationMap.get(postId);
       PostUpdateResponse response=contentmapper.toPostUpdateResponse(post);
        response.setMediaRepresentationList(mediaRepresentationList);
       return response;
    }

}
