package com.example.SocialMediaApp.Notification.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;


@Data
public class ContentEmail extends Email {
    private String postId;
    private String scheduledAt;
    public ContentEmail( List<Map<String, String>> to,String subject,String postId,String scheduledAt){
        super(to,subject);
        this.postId=postId;
        this.scheduledAt=scheduledAt;
    }
}
