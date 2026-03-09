package com.example.SocialMediaApp.Profile.domain.cache;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

// this class is used to cache and retrieve user important details to other users
//  it has longer ttl than the Profile class because its more accessed
@RedisHash(timeToLive =480,value = "ProfileInfo")
@Data
public class ProfileInfo {
    @Id
    private String userId;

    private String bio;
    private String avatarPath;
    private String username;
}
