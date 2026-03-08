package com.example.SocialMediaApp.SocialGraph.application.cache;

import com.example.SocialMediaApp.SocialGraph.application.FollowQueryHelper;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.domain.events.FollowAdded;
import com.example.SocialMediaApp.SocialGraph.domain.events.FollowRemoved;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowCacheUpdater {
    private final RedisTemplate<String,String> redisTemplate;
    public enum UpdateType{INCREMENT,DECREMENT}

    @EventListener
    public void addfollow(FollowAdded followAdded) {
        Follow follow=followAdded.getFollow();
        String followerId=follow.getFollower_id();
        String followingId =follow.getFollowing_id();
        boolean followersPage0Cached = redisTemplate.hasKey("user:" + followingId + ":followers:page0_cached");
        boolean followingsPage0Cached = redisTemplate.hasKey("user:" + followerId + ":followings:page0_cached");

        if (followersPage0Cached) {
            redisTemplate.opsForZSet().add(
                    "user:" + followingId + ":followers:",
                    followerId,
                    follow.getFollowDate().getEpochSecond()
            );
        }

        if (followingsPage0Cached) {
            redisTemplate.opsForZSet().add(
                    "user:" + followerId + ":followings:",
                    followingId,
                    follow.getFollowDate().getEpochSecond()
            );
        }


    }

    @EventListener
    public void removefollow(FollowRemoved followRemoved) {
        Follow follow=followRemoved.getFollow();
        String followerId=follow.getFollower_id();
        String followingId =follow.getFollowing_id();
        redisTemplate.opsForZSet().remove("user:"+followingId+":followers:",followerId);
        redisTemplate.opsForZSet().remove("user:"+followerId+":followings:",followingId);
        }

    public void UpdateCount(FollowQueryHelper.Position position,String userId,UpdateType updateType){
        int updateValue=updateType==UpdateType.INCREMENT?1:-1;
        // updating the follower and following count
        String key= FollowQueryHelper.Position.FOLLOWERS==position?"user:followers:":"user:followings:";
        if(redisTemplate.hasKey(key+userId)){
           Long count= redisTemplate.opsForValue().increment(key+userId,updateValue);
           log.info("new "+position.name()+" count "+count);
        }

    }
}
