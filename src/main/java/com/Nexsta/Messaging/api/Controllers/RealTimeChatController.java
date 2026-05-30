package com.Nexsta.Messaging.api.Controllers;


import com.Nexsta.Messaging.application.ChatActivityTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RealTimeChatController {

    private final ChatActivityTracker chatActivityTracker;


    @MessageMapping("/chat.enter/{chatId}")
    public void enterChat(@DestinationVariable String chatId, Principal principal) {
        chatActivityTracker.userEnteredChat(principal.getName(),chatId);
    }

    @MessageMapping("/chat.leave/{chatId}")
    public void leaveChat(@DestinationVariable String chatId, Principal principal) {
        chatActivityTracker.userLeftChat(principal.getName(),chatId);
    }

    @MessageMapping("/inbox.open")
    public void openInbox(Principal principal){
        chatActivityTracker.userOpenedInbox(principal.getName());
    }

    @MessageMapping("/typing/{chatId}")
    public void typingIndicator(@DestinationVariable String chatId, Principal principal){
        chatActivityTracker.deliverTyping(chatId,principal.getName());
    }
}
