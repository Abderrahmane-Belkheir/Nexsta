package com.Nexsta.Messaging.api.Controllers;

import com.Nexsta.Messaging.api.dto.ChatHearbeatDTO;
import com.Nexsta.Messaging.api.dto.SendMessageToChatDTO;
import com.Nexsta.Messaging.application.ChatActivityTracker;
import com.Nexsta.Messaging.application.ChatMessageService;
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
