package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatSummary {
    private String chatId;
    private ChatPreview preview;
    private String userId;
    // gro
    private String name;
    private String avatarUrl;
}
