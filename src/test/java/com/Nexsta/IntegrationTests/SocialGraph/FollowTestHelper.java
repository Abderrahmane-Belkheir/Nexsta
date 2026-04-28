package com.Nexsta.IntegrationTests.SocialGraph;

import com.Nexsta.Profile.domain.Profile;
import com.Nexsta.Profile.persistence.ProfileRepo;
import com.Nexsta.SocialGraph.application.FollowQueryHelper;
import com.Nexsta.SocialGraph.domain.Block;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.persistence.BlocksRepo;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import com.Nexsta.User.application.AuthenticatedUserService;
import com.Nexsta.User.domain.User;
import com.Nexsta.User.persistence.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class FollowTestHelper {

    private final FollowRepo followRepo;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;
    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;


    public String createUserProfile(FollowServiceIntegrationTest.ProfileType profiletype){
        String userId=UUID.randomUUID().toString();
        Profile profile=profiletype== FollowServiceIntegrationTest.ProfileType.PRIVATE?new Profile(true):new Profile(false);
        profile.setUser(new User(userId));
        profileRepo.save(profile);
        return userId;
    }

    public String createFollowRecord(Follow.Status status, FollowQueryHelper.Position position){
        String currentUserId=authenticatedUserService.getCurrentUser();
        String targetUserId=UUID.randomUUID().toString();
        Follow follow=position== FollowQueryHelper.Position.FOLLOWERS?
                new Follow(targetUserId,currentUserId,status):new Follow(currentUserId,targetUserId,status);
        followRepo.saveAndFlush(follow);
        return targetUserId;
    }

    public String createBlockRecord(boolean isCurrentBlocked){
        String currentUserId=authenticatedUserService.getCurrentUser();
        String targetUserId=UUID.randomUUID().toString();
        Block block=null;
        if(isCurrentBlocked){
             block=new Block(targetUserId,currentUserId);
        }else{
             block=new Block(currentUserId,targetUserId);
        }
        blocksRepo.saveAndFlush(block);
        return targetUserId;
    }
}
