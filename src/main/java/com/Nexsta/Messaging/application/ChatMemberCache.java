package com.Nexsta.Messaging.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMemberCache {

    private final RedisTemplate<String,String> redisTemplate;

    private static final String KEY = "chat:members:";
    private static final Duration TTL = Duration.ofMinutes(10);

    public void put(String chatId, List<String> memberIds) {
        String key = KEY + chatId;
        redisTemplate.delete(key);
        redisTemplate.opsForSet().add(key, memberIds.toArray(String[]::new));
        redisTemplate.expire(key, TTL);
    }

    public Optional<List<String>> get(String chatId) {
        Set<String> members = redisTemplate.opsForSet().members(KEY + chatId);
        if (members == null || members.isEmpty()) return Optional.empty();
        return Optional.of(new ArrayList<>(members));
    }

    public void evict(String chatId) {
        redisTemplate.delete(KEY + chatId);
    }
}
