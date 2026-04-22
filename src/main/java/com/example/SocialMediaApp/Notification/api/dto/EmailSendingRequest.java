package com.example.SocialMediaApp.Notification.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
 public class EmailSendingRequest {
    private Map<String, String> sender;
    private List<Map<String, String>> to;
    private String subject;
    private String htmlContent;
    private String scheduledAt;
    private String batchId;
}
