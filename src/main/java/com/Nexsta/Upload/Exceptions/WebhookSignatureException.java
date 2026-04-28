package com.Nexsta.Upload.Exceptions;

public class WebhookSignatureException extends RuntimeException {
    public WebhookSignatureException(String message) {
        super(message);
    }
}
