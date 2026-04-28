package com.Nexsta.Shared;

import com.Nexsta.Profile.persistence.ProfileRepo;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.persistence.BlocksRepo;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Aspect
public class VisibilityPolicy {

    private final BlocksRepo blocksRepo;
    private final ProfileRepo profileRepo;
    private final FollowRepo followRepo;



    public boolean isAllowed(String currentUserId,String requestedUserId){

        if(currentUserId.equals(requestedUserId)){
            return true;
        }

            boolean isBlocked=blocksRepo.existsByBlockerIdAndBlockedId(currentUserId,requestedUserId);
            if(isBlocked){
             return false;
            }
            boolean hasBlocked= blocksRepo.existsByBlockerIdAndBlockedId(requestedUserId,currentUserId);
            if (hasBlocked) {
              return false;
            }

            if(!profileRepo.existsByUserIdAndProfileSettingsIsPrivateFalse(requestedUserId)){
                if(!followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId,requestedUserId, Follow.Status.ACCEPTED)){
                  return false;
                }
            }
            return true;
        }


    }

