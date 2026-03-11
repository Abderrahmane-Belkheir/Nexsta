package com.example.SocialMediaApp.Upload.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UploadResponse {
    private String signedUrl;
    private String requestId;
}
