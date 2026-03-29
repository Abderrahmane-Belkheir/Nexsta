package com.example.SocialMediaApp.Shared;

import com.example.SocialMediaApp.Storage.StorageProperties;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Upload.domain.SupabaseWebhookPayload;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MediaUrlResolver {

    private final StorageProperties storageProperties;
    private final StorageService storageService;


    public String resolveUrl(String filepath) {
        return storageProperties.getUrl() + storageProperties.getEndpoint()+"/public/" + storageProperties.getPublicMediaBucket() + "/" + filepath;
    }

    public void  convertToSignedUrls(List<String> filePaths,Duration expiry) {
        Map<String,String> signedUrlsMap=storageService.generateBatchFetchSignedUrls(filePaths);
        filePaths.replaceAll(filePath->signedUrlsMap.getOrDefault(filePath,"URL_EXPIRED_OR_MISSING"));
    }
}
