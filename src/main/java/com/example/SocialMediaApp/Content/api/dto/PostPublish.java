package com.example.SocialMediaApp.Content.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
@Data
public class PostPublish {
    @NotBlank
    private String postId;
    private Instant scheduledAt;
}
