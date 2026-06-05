package com.Nexsta.Messaging.api.dto;

import lombok.Data;

@Data
public class RemoveMessage {
    private String messageId;
    private RemoveType removeType;

    public enum RemoveType{FOR_ME,FOR_EVERYONE}
}
