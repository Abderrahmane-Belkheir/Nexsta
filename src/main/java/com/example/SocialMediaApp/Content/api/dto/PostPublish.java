package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Validation.Annotations.ValidScheduled;
import lombok.Data;

import java.time.Instant;

@Data
public class PostPublish {
    @ValidScheduled
    private Instant scheduledAt;
}
