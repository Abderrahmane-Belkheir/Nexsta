package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Validation.Annotations.ValidScheduled;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
@Data
public class PostPublish {
    @NotBlank
    private String postId;
    @ValidScheduled
    private Instant scheduledAt;
}
