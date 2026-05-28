package com.Nexsta.Messaging.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboxEvent {

    private InboxEventType type;
    private String chatId;

    private List<String> readersId;

    private InboxEvent(InboxEventType type, String chatId) {
        this.type   = type;
        this.chatId = chatId;
    }

    public static InboxEvent newMessage(String chatId){
        return new InboxEvent(InboxEventType.NEW_MESSAGE, chatId);
    }

    public static InboxEvent readReceipt(String chatId, List<String> readersId) {
        InboxEvent e = new InboxEvent(InboxEventType.READ_RECEIPT, chatId);
        e.readersId = readersId;
        return e;
    }

    public enum InboxEventType{ NEW_MESSAGE,
        READ_RECEIPT}

}
