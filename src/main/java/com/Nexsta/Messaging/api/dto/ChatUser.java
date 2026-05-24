package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatUser {
    private String userId;
    private String username;
    private String avatarPath;
    private Boolean isActive;
    private String lastActivity;
}
