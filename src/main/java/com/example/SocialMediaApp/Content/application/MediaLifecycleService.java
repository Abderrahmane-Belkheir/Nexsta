package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.Story;
import com.example.SocialMediaApp.Content.persistence.MediaRepo;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import com.example.SocialMediaApp.Storage.StorageDir;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.MediaUpload;
import com.example.SocialMediaApp.Upload.domain.UploadFinalization;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
 class MediaLifecycleService {

    private final UploadGatewayService uploadGatewayService;
    private final MediaRepo mediaRepo;
    private final MediaUrlResolver mediaUrlResolver;
    private final Contentmapper contentmapper;

    public UploadFinalization extractMediaUploads(String userId, List<String> requestIds, UploadType uploadType){
        return uploadGatewayService.finalizeUploads(userId,requestIds,uploadType);
    }

    public List<Media> persistMedia(String destinationFolder,List<MediaUpload> mediaUploads, Post post){
        List<Media> mediaList=persistMediaHelper(destinationFolder,mediaUploads);
        mediaList.forEach(media -> media.setPost(post));
        return mediaRepo.saveAll(mediaList);
    }

    public List<Media> persistMedia(String destinationFolder,List<MediaUpload> mediaUploads, Story story){
        List<Media> mediaList=persistMediaHelper(destinationFolder,mediaUploads);
        mediaList.forEach(media -> media.setStory(story));
        return mediaRepo.saveAll(mediaList);
    }

    public String buildFolderPath(String useId,String postId,UploadType uploadType){
        return String.format("%s/%s/%s/%s", StorageDir.DRAFT.getDirName(),uploadType.toString().toLowerCase(),useId,postId);
    }

    private List<Media> persistMediaHelper(String destinationFolder,List<MediaUpload> mediaUploads){
        List<Media> mediaList=new ArrayList<>();
        for(int i=0;i<mediaUploads.size();i++){
            MediaUpload mediaUpload=mediaUploads.get(i);
            Media media= Media.builder().filepath(destinationFolder+"/"+mediaUpload.getId()).mediaType(mediaUpload.getMediaType()).displayOrder(i).build();
            mediaList.add(media);
        }
        return mediaList;
    }

    public Map<String,List<MediaRepresentation>> getPostsMedia(List<String> postIds,Post.PostStatus postStatus){
       Map<String,List<MediaRepresentation>> postMediaMap= mediaRepo.findByPostIdIn(postIds).stream().collect(Collectors.groupingBy(
               Media::getPostId,
               Collectors.mapping(media -> {
                   MediaRepresentation mediaRepresentation=contentmapper.toMediaRepresentation(media);
                 if(postStatus== Post.PostStatus.PUBLISHED)  mediaRepresentation.setFilepath(mediaUrlResolver.resolveUrl(media.getFilepath()));
                 return mediaRepresentation;
               },Collectors.toList())
       ));
       if(postStatus!= Post.PostStatus.PUBLISHED) mediaUrlResolver.convertToSignedUrls(postMediaMap);
       return postMediaMap;
    }

}
