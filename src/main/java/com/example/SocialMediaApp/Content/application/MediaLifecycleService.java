package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.Story;
import com.example.SocialMediaApp.Content.persistence.MediaRepo;
import com.example.SocialMediaApp.Storage.StorageDir;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.MediaUpload;
import com.example.SocialMediaApp.Upload.domain.UploadFinalization;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
 class MediaLifecycleService {

    private final UploadGatewayService uploadGatewayService;
    private final MediaRepo mediaRepo;

    public UploadFinalization extractMediaUploads(String userId, List<String> requestIds, UploadType uploadType){
        return uploadGatewayService.finalizeUploads(userId,requestIds,uploadType);
    }

    public List<Media> persistMedia(List<MediaUpload> mediaUploads, Post post){
        List<Media> mediaList=persistMediaHelper(mediaUploads);
        mediaList.forEach(media -> media.setPost(post));
        return mediaRepo.saveAll(mediaList);
    }

    public List<Media> persistMedia( List<MediaUpload> mediaUploads, Story story){
        List<Media> mediaList=persistMediaHelper(mediaUploads);
        mediaList.forEach(media -> media.setStory(story));
        return mediaRepo.saveAll(mediaList);
    }

    public String buildFolderPath(String useId,String postId,UploadType uploadType){
        return String.format("%s/%s/%s/%s", StorageDir.DRAFT.getDirName(),uploadType.toString().toLowerCase(),useId,postId);
    }

    private List<Media> persistMediaHelper(List<MediaUpload> mediaUploads){
        List<Media> mediaList=new ArrayList<>();
        for(int i=0;i<mediaUploads.size();i++){
            MediaUpload mediaUpload=mediaUploads.get(i);
            Media media= Media.builder().filepath(mediaUpload.getFilePath()).mediaType(mediaUpload.getMediaType()).displayOrder(i).build();
            mediaList.add(media);
        }
        return mediaList;
    }

}
