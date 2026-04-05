package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Validation.Annotations.ValidScheduled;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
@Data
public class PostPublish {
    @NotBlank
    private String postId;
    @Schema(
            description = "Scheduled publication time. Must be provided in UTC (ISO-8601). " +
                    "Constraints: Min 2 hours from now, Max 90 days from now."
    )
    @ValidScheduled
    private Instant scheduledAt;
}
