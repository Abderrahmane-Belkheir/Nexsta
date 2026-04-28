package com.Nexsta.Upload.domain;

import com.Nexsta.Content.domain.Media;
import lombok.*;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class UploadSession {
    private String userId;
    private UploadType uploadType;
    private String uploadRequestId;
    private String filePath;
    private Media.MediaType mediaType;
}
