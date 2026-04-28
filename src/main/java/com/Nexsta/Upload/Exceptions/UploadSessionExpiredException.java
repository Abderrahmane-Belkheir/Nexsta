
package com.Nexsta.Upload.Exceptions;

public class UploadSessionExpiredException extends RuntimeException {
    public UploadSessionExpiredException(String message) {
        super(message);
    }
}
