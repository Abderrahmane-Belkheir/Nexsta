package com.example.SocialMediaApp.Storage;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignedUploadRequest {
    private int expiresIn;
    public SignedUploadRequest(int expiresIn){
        this.expiresIn=60*expiresIn;
    }
}
