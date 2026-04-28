package com.Nexsta.Upload.Exceptions;

import lombok.Getter;

import java.util.List;
@Getter
public class UploadFailedException extends RuntimeException {
    private final List<String> failedUploadIds;
    public UploadFailedException(List<String> failedUploadIds) {
        this.failedUploadIds=failedUploadIds;
    }
}
