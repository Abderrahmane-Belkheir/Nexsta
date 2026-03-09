package com.example.SocialMediaApp.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatUser {
    private String userId;
    private String username;
    private String avatarPath;
    private Boolean online;
    private String lastActivity;
}
