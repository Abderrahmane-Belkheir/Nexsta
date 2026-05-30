package com.Nexsta.Messaging.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TypingEvent {
    private TypingEventType type;
    private String chatId;
    private String userId;

    public static TypingEvent start(String chatId, String userId){
        return new TypingEvent(TypingEventType.TYPING_START, chatId, userId);
    }

    public static TypingEvent stop(String chatId, String userId){
        return new TypingEvent(TypingEventType.TYPING_STOP, chatId, userId);
    }

    public enum TypingEventType { TYPING_START, TYPING_STOP }
}