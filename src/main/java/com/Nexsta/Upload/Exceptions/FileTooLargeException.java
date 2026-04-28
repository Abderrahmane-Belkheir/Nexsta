package com.Nexsta.Upload.Exceptions;

import lombok.Getter;

@Getter
public class FileTooLargeException extends RuntimeException {
    private long maxFileSize;
    public FileTooLargeException(long maxFileSize) {
        this.maxFileSize=maxFileSize;
    }
}
