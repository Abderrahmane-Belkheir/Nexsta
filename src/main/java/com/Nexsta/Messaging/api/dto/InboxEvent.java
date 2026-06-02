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

    // used for new chats
    private  ChatSummary chatSummary;

    private String message;

    private String typerId;

    private List<String> readersId;

    private InboxEvent(InboxEventType type, String chatId) {
        this.type   = type;
        this.chatId = chatId;
    }

    private InboxEvent(InboxEventType type, String chatId,List<String> readersId){
        this(type,chatId);
        this.readersId=readersId;
    }

    private InboxEvent(InboxEventType type,ChatSummary chatSummary){
        this(type, (String) null);
        this.chatSummary=chatSummary;
    }

    private InboxEvent(InboxEventType type, String chatId,String message) {
        this(type,chatId);
        this.message=message;
    }



    public static InboxEvent newMessage(String chatId){
        return new InboxEvent(InboxEventType.NEW_MESSAGE, chatId);
    }

    public static InboxEvent readReceipt(String chatId, List<String> readersId) {
        return new InboxEvent(InboxEventType.READ_RECEIPT,chatId,readersId);
    }

    public static InboxEvent newChat(ChatSummary chatSummary){
        return new InboxEvent(InboxEventType.NEW_CHAT,chatSummary);
    }

    public static  InboxEvent sentMessage(String chatId){
    return new InboxEvent(InboxEventType.SENT_MESSAGE,chatId);
    }

    public static InboxEvent receivedMessage(String chatId, String message){
      return  new InboxEvent(InboxEventType.RECEIVED_MESSAGE, chatId,message);
    }

    public static InboxEvent typingMessageStarted(String chatId,String typerId){
       InboxEvent e=new InboxEvent(InboxEventType.TYPING_MESSAGE_STARTED,chatId);
       e.typerId=typerId;
        return e;
    }

    public static InboxEvent typingMessageStopped(String chatId,String typerId){
        InboxEvent e=new InboxEvent(InboxEventType.TYPING_MESSAGE_STOPPED,chatId);
        e.typerId=typerId;
        return e;
    }

    public enum InboxEventType{ NEW_MESSAGE,
        READ_RECEIPT,NEW_CHAT,SENT_MESSAGE,RECEIVED_MESSAGE,TYPING_MESSAGE_STARTED,TYPING_MESSAGE_STOPPED}

}
