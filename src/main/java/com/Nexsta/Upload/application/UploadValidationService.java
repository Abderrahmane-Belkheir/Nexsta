package com.Nexsta.Upload.application;

import com.Nexsta.Upload.Exceptions.FileTooLargeException;
import com.Nexsta.Upload.Exceptions.UnsupportedMediaTypeException;
import com.Nexsta.Upload.api.dto.UploadRequest;
import com.Nexsta.Upload.domain.UploadType;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.Nexsta.Upload.domain.UploadType.*;


@Component
 class UploadValidationService {

    static final Map<UploadType,List<String>> supportedMediaTypes =Map.of(
            PROFILE, Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"),
            STORY, Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4",
            "video/quicktime" ),
            POST, Arrays.asList(
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "video/mp4",
                    "video/mpeg",
                    "video/quicktime"),
            THUMBNAIL,Arrays.asList("image/jpeg")
            );

    public static final long MAX_PROFILE_SIZE = 100 * 1024L;

    public static final long MAX_STORY_IMAGE_SIZE = 500 * 1024L;

    public static final long MAX_POST_IMAGE_SIZE = 1024 * 1024L;

    public static final long MAX_VIDEO_SIZE = 30 * 1024 * 1024L;

    public static final long MAX_THUMBNAIL_SIZE = 200 * 1024L;

    public void validateFile(UploadRequest request){
        UploadType uploadType=request.getUploadType();
     List<String> supportedMediaTypesForRequest = supportedMediaTypes.get(uploadType);
     boolean compatible=false;
     if(supportedMediaTypesForRequest !=null){
        compatible= supportedMediaTypesForRequest.stream().anyMatch(allowedTypes->allowedTypes.equals(request.getFileMimeType().toLowerCase()));
     }

        String filetMimeType =request.getFileMimeType();

     if(compatible){
         boolean isVideo =  filetMimeType.startsWith("video/");
         long limit=switch (uploadType) {

             case PROFILE -> MAX_PROFILE_SIZE;


             case POST -> isVideo
                     ? MAX_VIDEO_SIZE
                     : MAX_POST_IMAGE_SIZE;


             case STORY -> isVideo
                     ? MAX_VIDEO_SIZE
                     : MAX_STORY_IMAGE_SIZE;

             case THUMBNAIL -> MAX_THUMBNAIL_SIZE;
         };


         if(limit<request.getFileSize()){
             throw new FileTooLargeException(limit);
         }

         return;

     }

     throw new UnsupportedMediaTypeException(supportedMediaTypesForRequest);
    }

}
