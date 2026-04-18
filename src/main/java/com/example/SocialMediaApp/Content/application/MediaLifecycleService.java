package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.Story;
import com.example.SocialMediaApp.Content.persistence.MediaRepo;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import com.example.SocialMediaApp.Storage.StorageDir;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.MediaUpload;
import com.example.SocialMediaApp.Upload.domain.UploadFinalization;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
 class MediaLifecycleService {

    private final UploadGatewayService uploadGatewayService;
    private final MediaRepo mediaRepo;
    private final MediaUrlResolver mediaUrlResolver;

    public UploadFinalization extractMediaUploads(String userId, List<String> requestIds, UploadType uploadType){
        return uploadGatewayService.finalizeUploads(userId,requestIds,uploadType);
    }

    public List<Media> persistMedia(List<MediaUpload> mediaUploads, Post post){
        List<Media> mediaList=persistMediaHelper(mediaUploads);
        mediaList.forEach(media -> media.setPost(post));
        post.getMediaList().addAll(mediaList);
        return mediaList;
    }

    public List<Media> persistMedia(List<MediaUpload> mediaUploads, Story story){
        List<Media> mediaList=persistMediaHelper(mediaUploads);
        mediaList.forEach(media -> media.setStory(story));
        story.getMediaList().addAll(mediaList);
        return mediaList;
    }

    public String buildFolderPath(StorageDir storageDir,UploadType uploadType,String userId,String contentId){

        return String.format("%s/%s/%s/%s", storageDir.getDirName(),uploadType.toString().toLowerCase(), userId,contentId);
    }

    private List<Media> persistMediaHelper(List<MediaUpload> mediaUploads){
        List<Media> mediaList=new ArrayList<>();
        for(int i=0;i<mediaUploads.size();i++){
            MediaUpload mediaUpload=mediaUploads.get(i);
            Media media= Media.builder().id(mediaUpload.getId()).mediaType(mediaUpload.getMediaType()).displayOrder(i).build();
            mediaList.add(media);
        }
        return mediaList;
    }

    public Map<String,List<MediaRepresentation>> getPostsMedia(List<Post> posts, Post.PostStatus postStatus){
        Map<String,List<MediaRepresentation>> mediaReprentationMap=new HashMap<>();
        if(posts==null || posts.isEmpty()||postStatus== Post.PostStatus.DELETED) return mediaReprentationMap;
        posts.forEach(post -> {
          List<Media> mediaList=post.getMediaList();
          List<MediaRepresentation> mediaRepresentationList=mediaList.stream().map(media -> {
              MediaRepresentation mediaRepresentation=new MediaRepresentation(media.getId(),media.getMediaType());
              String mediaPath=mediaUrlResolver.resolvePath(post.getPostFolderPath(),media.getId());
              if(postStatus== Post.PostStatus.PUBLISHED) mediaRepresentation.setFilepath(mediaUrlResolver.resolveFullUrl(mediaPath));
              else mediaRepresentation.setFilepath(mediaPath);
              return mediaRepresentation;
          }).toList();
          mediaReprentationMap.put(post.getId(),mediaRepresentationList);
        });
        if(postStatus!= Post.PostStatus.PUBLISHED) mediaUrlResolver.convertToSignedUrls(mediaReprentationMap);
        return mediaReprentationMap;
    }

}
