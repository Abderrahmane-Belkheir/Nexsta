package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.api.dto.InboxEvent;
import com.Nexsta.Messaging.api.dto.MessageView;
import com.Nexsta.Messaging.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class RealTimeDeliveringService {

    private final SimpMessagingTemplate messagingTemplate;

    public void deliverMessage(List<String> usersId, MessageView messageView){
        for (String userId : usersId) {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/messages",
                    messageView
            );
        }
    }

    public void deliverInboxEvent(List<String> usersId, InboxEvent event){
        for (String userId:usersId){
            messagingTemplate.convertAndSendToUser(userId,
                    "/queue/preview",
                    event
            );
        }
    }

    public void deliverTyping(List<String> usersId,String typerId){
        for(String userId:usersId){
            messagingTemplate.convertAndSendToUser(userId,
                    "/queue/typing"
                    ,typerId
            );
        }
    }



    }


