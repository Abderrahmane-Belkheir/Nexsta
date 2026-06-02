package com.Nexsta.Messaging.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageDelivery.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = InboxDelivery.class, name = "INBOX"),
        @JsonSubTypes.Type(value = TypingDelivery.class,name = "TYPING")
})
public abstract class BaseDelivery {
}
