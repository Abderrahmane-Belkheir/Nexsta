package com.Nexsta.Messaging.domain;

import com.Nexsta.Messaging.api.dto.ChatSummary;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatAggregate {
    private Chat chat;
    private ChatSummary summary;
}
