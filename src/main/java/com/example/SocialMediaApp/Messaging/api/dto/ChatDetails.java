package com.example.SocialMediaApp.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class ChatDetails {
    private String chatId;
    private ChatUser chatUser;
    private List<MessageDTO> messages;
}
