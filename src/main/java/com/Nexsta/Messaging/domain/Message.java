package com.Nexsta.Messaging.domain;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "message")
@Data
public class Message {
    @Id
    private String id;
    @Indexed
    private String chatId;
    private String senderId;
    private MessageType messageType=MessageType.TEXT;
    private String content;
    private Instant sentAt= Instant.now();
    private Instant seenAt;
    private boolean deleted=false;
    public Message(String chatId,String senderId,String content){
        this.senderId=senderId;
        this.content=content;
        this.chatId=chatId;
    }

}
