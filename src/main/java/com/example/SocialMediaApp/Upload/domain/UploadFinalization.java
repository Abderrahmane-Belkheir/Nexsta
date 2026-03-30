package com.example.SocialMediaApp.Upload.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@AllArgsConstructor
@Getter
public class UploadFinalization {
    private final List<MediaUpload> mediaUploads;
    private final List<String> filePaths;
}
