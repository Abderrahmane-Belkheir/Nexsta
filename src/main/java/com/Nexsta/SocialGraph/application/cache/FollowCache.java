package com.Nexsta.SocialGraph.application.cache;

import com.Nexsta.SocialGraph.application.FollowQueryHelper;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowCache {
    private final RedisTemplate<String,String> redisTemplate;
    private final FollowRepo followRepo;
    public enum UpdateType{INCREMENT,DECREMENT}


    public void UpdateCount(FollowQueryHelper.Position position,String userId,UpdateType updateType){
        int updateValue=updateType==UpdateType.INCREMENT?1:-1;
        // updating the follower and following count
        String key= FollowQueryHelper.Position.FOLLOWERS==position?"user:followers:":"user:followings:";
        if(redisTemplate.hasKey(key+userId)){
           Long count= redisTemplate.opsForValue().increment(key+userId,updateValue);
        }

    }

    public String UserFollowerCount(String targetUserId){
        String countCache=redisTemplate.opsForValue().get("user:followers:"+ targetUserId);
        if(countCache!=null){
            log.info("getting the followers count from the cache");
            return countCache;
        }

        log.info("getting the followers count from db and caching");
        long count=followRepo.countByFollowingIdAndStatus(targetUserId, Follow.Status.ACCEPTED);
        redisTemplate.opsForValue().set("user:followers:"+ targetUserId,String.valueOf(count));
        redisTemplate.expire("user:followers:"+ targetUserId,20, TimeUnit.MINUTES);
        return String.valueOf(count);
    }

    public String UserFollowingCount(String targetUserId){
        String countCache=redisTemplate.opsForValue().get("user:followings:"+ targetUserId);
        if(countCache!=null){
            log.info("getting the followings count from the cache");
            return countCache;
        }
        log.info("getting the followings count from db and caching");
        long count=followRepo.countByFollowerIdAndStatus(targetUserId, Follow.Status.ACCEPTED);
        redisTemplate.opsForValue().set("user:followings:"+ targetUserId,String.valueOf(count));
        redisTemplate.expire("user:followings:"+ targetUserId,20,TimeUnit.MINUTES);
        return String.valueOf(count);
    }

}
