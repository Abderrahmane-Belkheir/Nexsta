package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.PostPreview;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.UploadSession;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThumbnailService {

    private final UploadGatewayService uploadGatewayService;

    public PostPreview generatePostThumbnail(String userId, String thumbnailId, Media firstMedia){
        UploadSession uploadSession=uploadGatewayService.finalizeUpload(userId,thumbnailId, UploadType.THUMBNAIL);
        return new PostPreview(firstMedia.getMediaType(),thumbnailId,uploadSession.getFilePath());
    }

}
