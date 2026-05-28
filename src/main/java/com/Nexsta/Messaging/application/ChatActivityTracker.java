package com.Nexsta.Messaging.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
@RequiredArgsConstructor
@Slf4j
public class ChatActivityTracker {

    private final RedisTemplate<String,String> redisTemplate;

    private static final String CHAT_ACTIVE_KEY = "chat:active:";
    private static final String INBOX_ACTIVE_KEY = "inbox:active";
    private static final Duration TTL = Duration.ofSeconds(30);


    public void userEnteredChat(String userId, String chatId) {
        redisTemplate.opsForValue().set(
                CHAT_ACTIVE_KEY + chatId + ":" + userId,
                "1",
                TTL
        );
    }

    public void userLeftChat(String userId, String chatId) {
        redisTemplate.delete(CHAT_ACTIVE_KEY + chatId + ":" + userId);
    }

    public boolean isUserActiveInChat(String userId, String chatId) {
        return redisTemplate.hasKey(CHAT_ACTIVE_KEY + chatId + ":" + userId);
    }

    public void userOpenedInbox(String userId) {
        redisTemplate.opsForValue().set(
                INBOX_ACTIVE_KEY + userId,
                "1",
                TTL
        );
    }

    public void userClosedInbox(String userId) {
        redisTemplate.delete(INBOX_ACTIVE_KEY + userId);
    }

    public boolean isUserActiveInInbox(String userId) {
        return redisTemplate.hasKey(INBOX_ACTIVE_KEY + userId);
    }


}






