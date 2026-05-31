package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.api.dto.BaseDelivery;
import com.Nexsta.Messaging.api.dto.InboxDelivery;
import com.Nexsta.Messaging.api.dto.MessageDelivery;
import com.Nexsta.Messaging.api.dto.MessageView;
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
                    log.info("message {}", m);
                    realTimeDeliveringService.deliverMessage(m);
                }else if(baseDelivery instanceof InboxDelivery i){
                    log.info("inbox {}", i);
                    realTimeDeliveringService.deliverInboxEvent(i);
                }

            } catch (JsonProcessingException e) {
                log.error("Failed to parse chat message", e);
            }
        }
    }

