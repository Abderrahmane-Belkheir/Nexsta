package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Upload.Exceptions.WebhookSignatureException;
import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.domain.SupabaseWebhookPayload;
import com.example.SocialMediaApp.Upload.domain.UploadSession;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookVerification {

    @Value("${supabase.webhook.secret}")
    private String webhookSecret;
    private final UploadValidationService uploadValidationService;
    private final RedisTemplate<String,String> redisTemplate;

    public void verifySignature(String signature) {
        if (signature == null || !signature.equals(webhookSecret)) {
            throw new WebhookSignatureException("Invalid webhook signature");
        }
        log.error("webhook validation passed");
    }

    public void verifyFileUploaded(UploadSession uploadSession, SupabaseWebhookPayload.StorageRecord storageRecord){
        Map<String,Object> metaData=storageRecord.getMetadata();
        String fileMimeType = (String) metaData.get("mimetype");
        Long fileSize = ((Number) metaData.get("size")).longValue();
        UploadType uploadType=uploadSession.getUploadType();
        uploadValidationService.validateFile(new UploadRequest(uploadType,fileMimeType,fileSize,0));
        Media.MediaType mediaType= determineMediaType(fileMimeType);
        uploadSession.setMediaType(mediaType);
    }

    private Media.MediaType determineMediaType(String mimeType) {
        if (mimeType == null) {
           // throw new UnsupportedMediaTypeException("Mimetype is missing from storage record");
        }

        if (mimeType.startsWith("image/")) {
            return Media.MediaType.IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return Media.MediaType.VIDEO;
        } else {
           // throw new UnsupportedMediaTypeException("Unsupported file format: " + mimeType);
        }
        return null;
    }
}
