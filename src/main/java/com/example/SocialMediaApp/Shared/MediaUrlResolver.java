package com.example.SocialMediaApp.Shared;

import com.example.SocialMediaApp.Storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class MediaUrlResolver {

    private final StorageProperties storageProperties;

    @Named("resolveUrl")
    public String resolveUrl(String filepath) {
        return storageProperties.getUrl() + "/storage/v1/object/public/" + storageProperties.getMediaBucket() + "/" + filepath;
    }

    public String resolveSignedUrl(String filepath, Duration expiry) {
        // if signed URLs for private buckets is needed.
        return "";
    }
}
