package com.example.SocialMediaApp.Notification.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Builder
@Data
public class EmailSending {
    private List<Map<String, String>> to;
    private String subject;
    private String htmlContent;
    private String scheduledAt;
}
