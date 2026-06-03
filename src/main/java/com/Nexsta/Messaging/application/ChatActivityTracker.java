package com.Nexsta.Messaging.application;


import com.Nexsta.Messaging.persistence.ChatRepo;
import com.Nexsta.Shared.ServerInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;



@Component
@RequiredArgsConstructor
@Slf4j
public class ChatActivityTracker {

    private final RedisTemplate<String,String> redisTemplate;
    private final RedisTemplate<String,Object> objectRedisTemplate;
    private final RealTimeDeliveringService realTimeDeliveringService;
    private final ChatRepo chatRepo;
    private static final String CHAT_ACTIVE_KEY = "chat:active:";
    private static final String INBOX_ACTIVE_KEY = "inbox:active:";
    private static final Duration TTL = Duration.ofSeconds(10);
    private final ServerInstance serverInstance;

    public void userEnteredChat(String userId, String chatId) {
        redisTemplate.opsForValue().set(
                CHAT_ACTIVE_KEY + chatId + ":" + userId,
                serverInstance.getInstanceId(),
                TTL
        );
    }

    public void userLeftChat(String userId, String chatId) {
        redisTemplate.delete(CHAT_ACTIVE_KEY + chatId + ":" + userId);
    }

    public Optional<String> isUserActiveInChat(String userId, String chatId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(CHAT_ACTIVE_KEY + chatId + ":" + userId));
    }

    public void userOpenedInbox(String userId) {
        log.info("user {} opened chat {} ",userId,serverInstance.getInstanceId());
        redisTemplate.opsForValue().set(
                INBOX_ACTIVE_KEY + userId,
                serverInstance.getInstanceId(),
                TTL
        );
    }

    public Optional<String> isUserActiveInInbox(String userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(INBOX_ACTIVE_KEY + userId));
    }


}




