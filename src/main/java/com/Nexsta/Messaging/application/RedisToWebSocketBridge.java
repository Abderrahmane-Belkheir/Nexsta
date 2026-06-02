package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.api.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RedisToWebSocketBridge implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RealTimeDeliveringService realTimeDeliveringService;

        @Override
        public void onMessage(Message message, byte[] pattern) {
            try {
                String body = new String(message.getBody());

                BaseDelivery baseDelivery =objectMapper.readValue(body,BaseDelivery.class);

                if (baseDelivery instanceof MessageDelivery m) {
                    realTimeDeliveringService.deliverMessage(m);
                }else if(baseDelivery instanceof InboxDelivery i){
                    realTimeDeliveringService.deliverInboxEvent(i);
                } else if (baseDelivery instanceof TypingDelivery t) {
                    realTimeDeliveringService.deliverTypingEvent(t);
                }

            } catch (JsonProcessingException e) {
                log.error("Failed to parse chat message", e);
            }
        }
    }

