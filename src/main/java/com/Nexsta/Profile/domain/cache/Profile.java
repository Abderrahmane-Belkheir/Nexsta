package com.Nexsta.Profile.domain.cache;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

//this class is used to cache the profile record from the db it has all the info of the actual record its used by the owing user
// it has shorter ttl than ProfileInfo class because its less accessed (only in profile updating)
@RedisHash(timeToLive =240,value="Profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    private String uuid;

    private String bio;
    private String username;
    private String privateavatarurl;
    private String publicavatarurl;
    private boolean showifonline=false;
    private boolean isprivate=false;
    private String userId;
}
