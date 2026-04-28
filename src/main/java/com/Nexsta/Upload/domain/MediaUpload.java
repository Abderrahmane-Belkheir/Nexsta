package com.Nexsta.Upload.domain;

import com.Nexsta.Content.domain.Media;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MediaUpload {
    private String id;
    private Media.MediaType mediaType;
}
