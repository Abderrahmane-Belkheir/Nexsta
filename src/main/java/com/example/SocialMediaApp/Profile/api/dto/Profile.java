package com.example.SocialMediaApp.Profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Profile {

    @Size(max = 30)
    private String bio;
    @NotBlank
    private String username;
}
