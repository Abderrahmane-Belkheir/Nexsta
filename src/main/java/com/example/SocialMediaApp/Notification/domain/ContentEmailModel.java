package com.example.SocialMediaApp.Notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public  class ContentEmailModel {
    private String postId;
    private String scheduledAt;
    private String redirectUrl;
}
