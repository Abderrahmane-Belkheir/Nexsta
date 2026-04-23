package com.example.SocialMediaApp.Notification.domain;


import lombok.Data;
import lombok.Getter;


import java.util.List;
import java.util.Map;


@Getter
public class ContentEmail extends Email {
    private String postId;
    public ContentEmail( List<Map<String, String>> to,String subject,String postId,String scheduledAt){
        super(to,subject,scheduledAt);
        this.postId=postId;
    }
}
