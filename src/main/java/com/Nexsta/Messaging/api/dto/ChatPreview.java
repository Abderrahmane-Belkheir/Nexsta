package com.Nexsta.Messaging.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatPreview {

    private ChatPreviewType previewType;

     private int unreadCount;

     private Instant sentAt;

     private String message;

     private List<String> seenByUserIds;

     private Instant seenAt;


     public static ChatPreview unread(int count) {
         return new ChatPreview(ChatPreviewType.UNREAD, count, null,null,null,null);
     }

     public static ChatPreview seen(List<String> seenByUserIds,Instant seenAt) {
         if(seenByUserIds.size()>1) seenAt=null;
         return new ChatPreview(ChatPreviewType.SEEN, 0,null,null,seenByUserIds,seenAt);
     }

     public static ChatPreview sent(Instant sentAt) {
         return new ChatPreview(ChatPreviewType.SENT, 0,sentAt,null, null,null);
     }

     public static ChatPreview received(String message){
         return new ChatPreview(ChatPreviewType.RECEIVED,0,null,message,null,null);
     }

     public enum ChatPreviewType{
        UNREAD,
        SEEN,
        SENT,
         RECEIVED
        }

    }

