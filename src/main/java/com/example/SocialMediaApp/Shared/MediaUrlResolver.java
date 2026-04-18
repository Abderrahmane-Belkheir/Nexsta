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
    private static final int signedUrlExpiry=5;

    public  String resolvePath(String folder, String mediaId) {
        return String.format("%s/%s",folder,mediaId);
    }

    public String resolveFullUrl(String path){
        return String.format("%s%s/%s/%s/%s",storageProperties.getUrl(),storageProperties.getEndpoint(),"public",storageProperties.getPublicMediaBucket(),path);
    }
    // specifically used for generating thumbnail of non published posts

    public String generateSignerUrl(String p){
        Map<String,String> s= storageService.generateBatchFetchSignedUrls(List.of(p),signedUrlExpiry);
       return String.format("%s%s%s",storageProperties.getUrl(),storageProperties.getEndpoint().replace("/object",""),s.getOrDefault(p,"URL_EXPIRED_OR_MISSING"));
    }

    public void convertToSignedUrls(Map<String,List<MediaRepresentation>> map){
        List<String> filePaths= map.values().stream().flatMap(Collection::stream).map(MediaRepresentation::getFilepath).toList();
        Map<String,String> signedUrlsMap=storageService.generateBatchFetchSignedUrls(filePaths,signedUrlExpiry);

        for(Map.Entry<String,List<MediaRepresentation>> mapEntry:map.entrySet()){
            mapEntry.getValue().
                    forEach(mediaRepresentation->
                            mediaRepresentation.setFilepath(String.format("%s%s%s",storageProperties.getUrl(),storageProperties.getEndpoint().replace("/object",""),signedUrlsMap.getOrDefault(mediaRepresentation.getFilepath(),"URL_EXPIRED_OR_MISSING"))));
        }

    }


}
