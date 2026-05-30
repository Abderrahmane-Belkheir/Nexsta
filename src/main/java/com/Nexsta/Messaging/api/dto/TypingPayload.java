package com.Nexsta.Messaging.api.dto;

import lombok.Data;

@Data
public class TypingPayload {
    private TypingEvent.TypingEventType typingEventType;
}
