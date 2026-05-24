package com.Nexsta.Messaging.api.dto;

import com.Nexsta.Messaging.domain.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ChatDetails {
    private String chatId;
    private Chat.ChatType chatType;


    private String chatName;
    private String chatAvatar;

    private List<ChatUser>  chatUsers;

}
