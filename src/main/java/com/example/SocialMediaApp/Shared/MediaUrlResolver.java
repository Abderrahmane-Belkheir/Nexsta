package com.example.SocialMediaApp.Shared;

import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Storage.StorageProperties;
import com.example.SocialMediaApp.Storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MediaUrlResolver {

    private final StorageProperties storageProperties;
    private final StorageService storageService;


    public  String resolvePath(String folder, String mediaId) {
        return String.format("%s/%s",folder,mediaId);
    }

    public String resolveFullUrl(String path){
        return String.format("%s%s/%s/%s/%s",storageProperties.getUrl(),storageProperties.getEndpoint(),"public",storageProperties.getPublicMediaBucket(),path);
    }

    public void convertToSignedUrls(Map<String,List<MediaRepresentation>> map){
        List<String> filePaths= map.values().stream().flatMap(Collection::stream).map(MediaRepresentation::getFilepath).toList();
        Map<String,String> signedUrlsMap=storageService.generateBatchFetchSignedUrls(filePaths,5);

        for(Map.Entry<String,List<MediaRepresentation>> mapEntry:map.entrySet()){
            mapEntry.getValue().
                    forEach(mediaRepresentation->
                            mediaRepresentation.setFilepath(String.format("%s%s%s",storageProperties.getUrl(),storageProperties.getEndpoint().replace("/object",""),signedUrlsMap.getOrDefault(mediaRepresentation.getFilepath(),"URL_EXPIRED_OR_MISSING"))));
        }

    }


}
