package com.Nexsta.Notification.domain;


import lombok.Getter;


@Getter
public class ContentEmail extends Email {
    private String postId;
    public ContentEmail( String to,String subject,String postId,String scheduledAt){
        super(to,subject,scheduledAt);
        this.postId=postId;
    }
}
