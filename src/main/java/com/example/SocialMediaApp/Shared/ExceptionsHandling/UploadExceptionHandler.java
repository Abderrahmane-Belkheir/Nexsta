package com.example.SocialMediaApp.Shared.ExceptionsHandling;

import com.example.SocialMediaApp.Upload.Exceptions.FileTooLargeException;
import com.example.SocialMediaApp.Upload.Exceptions.UnsupportedMediaTypeException;
import com.example.SocialMediaApp.Upload.Exceptions.UploadFailedException;
import com.example.SocialMediaApp.Upload.api.dto.UnsupportedMediaTypeResponse;
import com.example.SocialMediaApp.Upload.api.dto.UploadFailedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(UploadFailedException.class)
    public ResponseEntity<UploadFailedResponse>  handleFailedUploads(UploadFailedException e){
        List<String> failedUploadIds=e.getFailedUploadIds();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new UploadFailedResponse("some files failed uploading :",failedUploadIds));
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<Map<String,Long>>  handleFileSize(FileTooLargeException e){
        return ResponseEntity.status(413).body(Map.of("max file size: ",e.getMaxFileSize()));
    }

    @ExceptionHandler(UnsupportedMediaTypeException.class)
    public ResponseEntity<UnsupportedMediaTypeResponse> handleUnsupportedMedia(UnsupportedMediaTypeException e){
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(new UnsupportedMediaTypeResponse("Unsupported Media Type",e.getSupportedMediaType()));
    }

}
