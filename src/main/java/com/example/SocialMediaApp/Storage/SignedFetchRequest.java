package com.example.SocialMediaApp.Storage;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class SignedFetchRequest {
    private List<String> paths;
    private int expiresIn;
    public SignedFetchRequest(List<String> paths,int expiresIn){
        this.paths=paths;
        this.expiresIn=60*expiresIn;
    }

}
