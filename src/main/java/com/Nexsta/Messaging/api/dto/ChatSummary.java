package com.Nexsta.Messaging.api.dto;

import com.Nexsta.Messaging.domain.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatSummary {
    private String chatId;
    private Chat.ChatType chatType;
    private ChatPreview preview;

    // one to one chat only
    private boolean active;
    private String lastSeen;

    private String chatName;
    private String chatAvatar;
}
