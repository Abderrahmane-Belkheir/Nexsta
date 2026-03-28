package com.example.SocialMediaApp.Storage;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SignedFetchResponse {
    private String path;
    private String signedURL;
}
