package com.Nexsta.User.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserActivityTracker {

    @Value("${instanceId")
    private String instanceId;

    private final RedisTemplate<String,String> redisTemplate;

    public void setUserActive(String userId){redisTemplate.opsForValue().set("presence:user: "+userId,instanceId,30,TimeUnit.SECONDS);}

    public void setUserLastSeen(String userId){redisTemplate.opsForValue().set("user lastSeen:"+userId, Instant.now().toString());}

    public String getUserLastSeen(String userId){
        return redisTemplate.opsForValue().get("user lastSeen:"+userId);
    }

    public boolean getUserStatus(String userId){
        return redisTemplate.hasKey("presence:user: "+ userId);
    }

    public String getPresentUserInstance(String userId){return redisTemplate.opsForValue().get("presence:user: "+userId);}

}
