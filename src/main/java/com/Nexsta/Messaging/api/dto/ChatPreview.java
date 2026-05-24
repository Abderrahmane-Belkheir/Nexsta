package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatPreview {

     private ChatPreviewType type;

     // only populated when type == UNREAD
     private int unreadCount;

     private String message;

     // only populated when type == SEEN
     private List<String> seenByUserIds;
     private Instant seenAt;


     public static ChatPreview unread(int count) {
         return new ChatPreview(ChatPreviewType.UNREAD, count, null,null,null);
     }

     public static ChatPreview seen(List<String> seenByUserIds,Instant seenAt) {
         if(seenByUserIds.size()>1) seenAt=null;
         return new ChatPreview(ChatPreviewType.SEEN, 0,null,seenByUserIds,seenAt);
     }

     public static ChatPreview sent() {
         return new ChatPreview(ChatPreviewType.SENT, 0,null, null,null);
     }

     public static ChatPreview received(String message){
         return new ChatPreview(ChatPreviewType.RECEIVED,0,message,null,null);
     }

     public enum ChatPreviewType{
        UNREAD,
        SEEN,
        SENT,
         RECEIVED
        }

    }

