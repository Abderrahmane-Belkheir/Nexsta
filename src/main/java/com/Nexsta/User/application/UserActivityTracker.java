package com.Nexsta.User.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserActivityTracker {

    private final RedisTemplate<String,String> redisTemplate;

    public void setUserActive( String userId){
        redisTemplate.opsForValue().set("user: "+userId,"online");
        redisTemplate.expire("user: "+userId,30, TimeUnit.SECONDS);
    }


    public void setUserLastSeen(String userId){
        redisTemplate.opsForValue().set("user lastSeen:"+userId, Instant.now().toString());
    }

    public String getUserLastSeen(String userId){
        return redisTemplate.opsForValue().get("user lastSeen:"+userId);
    }

    public boolean getUserStatus(String userId){
        return redisTemplate.hasKey("user: "+ userId);
    }
}
