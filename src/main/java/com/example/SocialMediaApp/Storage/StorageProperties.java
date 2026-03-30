package com.example.SocialMediaApp.Storage;

import jdk.jfr.DataAmount;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix ="supabase" )
@Component
@Data
@Primary
public class StorageProperties {
    private String url;
    private String apiKey;
    private String endpoint;
    private String PublicMediaBucket;
    private String PrivateMediaBucket;
    private String MoveBetweenBucketsEndpoint;
    private String MoveInSameBucketsEndpoint;
    private String MoveBatchTempFilesEndpoint;
}
