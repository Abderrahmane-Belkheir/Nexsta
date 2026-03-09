package com.example.SocialMediaApp.Storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignRequest {
    private int expiresIn;
    public SignRequest(int expiresIn){
        this.expiresIn=60*expiresIn;
    }
}
