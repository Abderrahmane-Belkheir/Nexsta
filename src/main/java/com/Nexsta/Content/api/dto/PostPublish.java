package com.Nexsta.Content.api.dto;

import com.Nexsta.Validation.Annotations.ValidScheduled;
import lombok.Data;

import java.time.Instant;

@Data
public class PostPublish {
    @ValidScheduled
    private Instant scheduledAt;
}
