package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MessageDeliveringService {

    private final SimpMessagingTemplate messagingTemplate;

    public void deliverMessage(List<String> usersId, Message message){
        for (String userId : usersId) {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/messages",
                    message
            );
        }
    }
    }


