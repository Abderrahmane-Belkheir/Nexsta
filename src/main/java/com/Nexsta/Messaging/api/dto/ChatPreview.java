package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

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


     public static ChatPreview unread(int count) {
         return new ChatPreview(ChatPreviewType.UNREAD, count, null,null);
     }

     public static ChatPreview seen(List<String> seenByUserIds) {
         return new ChatPreview(ChatPreviewType.SEEN, 0,null,seenByUserIds);
     }

     public static ChatPreview sent() {
         return new ChatPreview(ChatPreviewType.SENT, 0,null, null);
     }

     public static ChatPreview received(String message){
         return new ChatPreview(ChatPreviewType.RECEIVED,0,message,null);
     }

     public enum ChatPreviewType{
        UNREAD,
        SEEN,
        SENT,
         RECEIVED
        }

    }

