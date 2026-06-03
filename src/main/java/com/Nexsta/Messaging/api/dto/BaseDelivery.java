package com.Nexsta.Messaging.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageDelivery.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = InboxDelivery.class, name = "INBOX"),
        @JsonSubTypes.Type(value = TypingDelivery.class,name = "TYPING")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseDelivery {
    private List<String> receivers;
}
