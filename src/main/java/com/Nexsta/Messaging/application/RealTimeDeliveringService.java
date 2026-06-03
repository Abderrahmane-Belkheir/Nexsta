package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.api.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RealTimeDeliveringService {

    private final SimpMessagingTemplate messagingTemplate;

    public void deliverMessage(MessageDelivery messageDelivery){
        MessageView message=messageDelivery.getMessage();
        for (String receiverId : messageDelivery.getReceivers()) {
            messagingTemplate.convertAndSendToUser(
                   receiverId,
                    "/queue/messages",
                   message
            );
        }
    }

    public void deliverInboxEvent(InboxDelivery inboxDelivery){
        InboxEvent event=inboxDelivery.getEvent();
        for (String receiverId:inboxDelivery.getReceivers()){
            messagingTemplate.convertAndSendToUser(receiverId,
                    "/queue/preview",
                    event
            );
        }
    }

        public void deliverTypingEvent(TypingDelivery typingDelivery){
        TypingEvent event=typingDelivery.getEvent();
        for(String userId:typingDelivery.getReceivers()){
            messagingTemplate.convertAndSendToUser(userId,
                    "/queue/typing"
                    ,event
            );
        }
    }

    public void deliverMessageRemoved(MessageRemovedDelivery messageRemovedDelivery){
        String messageId= messageRemovedDelivery.getMessageId();
        for(String userId:messageRemovedDelivery.getReceivers()){
            messagingTemplate.convertAndSendToUser(userId,
                    "/queue/message.removed"
                    ,messageId
            );
        }
    }

    }


