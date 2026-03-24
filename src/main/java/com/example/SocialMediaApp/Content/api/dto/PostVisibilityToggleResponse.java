package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostVisibilityToggleResponse {

    private  PostStatus status;

    public enum PostStatus{PUBLISHED,UNPUBLISHED}
}
