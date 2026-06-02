package com.Nexsta.Messaging.api.Controllers;


import com.Nexsta.Messaging.api.dto.TypingEvent;
import com.Nexsta.Messaging.api.dto.TypingPayload;
import com.Nexsta.Messaging.application.ChatActivityTracker;
import com.Nexsta.Messaging.application.TypingEventRelay;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RealTimeChatController {

    private final ChatActivityTracker chatActivityTracker;
    private final TypingEventRelay typingEventRelay;


    @MessageMapping("/chat.enter/{chatId}")
    public void enterChat(@DestinationVariable String chatId, Principal principal) {
        chatActivityTracker.userEnteredChat(principal.getName(),chatId);
    }

    @MessageMapping("/chat.leave/{chatId}")
    public void leaveChat(@DestinationVariable String chatId, Principal principal) {
        chatActivityTracker.userLeftChat(principal.getName(),chatId);
    }

    @MessageMapping("/chat.typing/{chatId}")
    public void typingStart(@DestinationVariable String chatId, Principal principal, @Payload TypingPayload payload){
        typingEventRelay.deliverTypingEvent(chatId,principal.getName(), payload);
    }

    @MessageMapping("/inbox.open")
    public void openInbox(Principal principal){
        chatActivityTracker.userOpenedInbox(principal.getName());
    }


}
