package com.Nexsta.Messaging.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ChatActivityTracker {

    private final RedisTemplate<String,String> redisTemplate;

    public void setChat_UserStatus(String userId,String chatId){
        redisTemplate.opsForValue().set(userId+":"+chatId,"active",10,TimeUnit.SECONDS);
    }

    public boolean getChat_UserStatus(String userId,String chatId){
        return redisTemplate.hasKey(userId+":"+chatId);
    }

}



