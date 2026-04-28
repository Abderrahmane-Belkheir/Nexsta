package com.Nexsta.Content.application;

import com.Nexsta.Content.domain.Media;
import com.Nexsta.Content.domain.PostPreview;
import com.Nexsta.Upload.application.UploadGatewayService;
import com.Nexsta.Upload.domain.UploadSession;
import com.Nexsta.Upload.domain.UploadType;
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
