package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatSummary {
    private String chatId;
    private String chatPreview;
    private String userId;
    private String username;
    private String avatarurl;
}
