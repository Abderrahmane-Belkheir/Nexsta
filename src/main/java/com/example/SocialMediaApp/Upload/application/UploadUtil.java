package com.example.SocialMediaApp.Upload.application;

import com.example.SocialMediaApp.Storage.StorageDir;
import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.domain.UploadInitiation;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Service
@RequiredArgsConstructor
 class UploadUtil {

    public UploadInitiation generateUploadResponse(String userId, UploadRequest request){
        String uploadRequestId = UUID.randomUUID().toString();
        UploadType uploadType=request.getUploadType();
        return new UploadInitiation(String.format("%s/%s/%s/%s", StorageDir.TEMPORARY.getDirName(),uploadType.toString().toLowerCase(),userId,uploadRequestId),uploadRequestId);
    }

    public UploadRequest toUploadRequest(MultipartFile file){
        UploadRequest request = new UploadRequest();
        request.setFileMimeType(file.getContentType());
        request.setFileSize(file.getSize());
        request.setUploadType(UploadType.PROFILE);
        return request;
    }
}

