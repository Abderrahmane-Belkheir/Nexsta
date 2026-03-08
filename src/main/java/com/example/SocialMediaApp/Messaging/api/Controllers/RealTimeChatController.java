package com.example.SocialMediaApp.Messaging.api.Controllers;

import com.example.SocialMediaApp.Messaging.api.dto.ChatHearbeatDTO;
import com.example.SocialMediaApp.Messaging.api.dto.SendMessageToChatDTO;
import com.example.SocialMediaApp.Messaging.application.ChatActivityTracker;
import com.example.SocialMediaApp.Messaging.application.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RealTimeChatController {

    private final ChatActivityTracker chatActivityTracker;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.join")
    public void join(Principal principal, ChatHearbeatDTO chatHearbeatDTO){
        chatActivityTracker.setChat_UserStatus(principal.getName(),chatHearbeatDTO.getChatId());
    }

    @MessageMapping("chat.send")
    public void sendMessage(Principal principal, SendMessageToChatDTO sendMessageToChatDTO){
        chatMessageService.sendMessageToChat(principal,sendMessageToChatDTO);
    }

}
