package com.example.SocialMediaApp.Content.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentCreationRequest {
    @NotBlank
    @Size(max = 500)
    private String content;
}
