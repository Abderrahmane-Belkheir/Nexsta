package com.Nexsta.Profile.application;

import com.Nexsta.Profile.api.dto.ProfileSettingsDto;
import com.Nexsta.SocialGraph.application.cache.FollowCacheWriter;
import com.Nexsta.User.application.AuthenticatedUserService;
import com.Nexsta.Profile.api.dto.ProfileDetails;
import com.Nexsta.Profile.application.cache.ProfileCacheManager;
import com.Nexsta.Profile.domain.Profile;


import com.Nexsta.Profile.domain.cache.ProfileInfo;
import com.Nexsta.Profile.persistence.ProfileRepo;
import com.Nexsta.Shared.CheckUserExistence;
import com.Nexsta.Shared.Mappers.Profilemapper;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.domain.RelationshipStatus;
import com.Nexsta.SocialGraph.persistence.BlocksRepo;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileQueryService {

    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileCacheManager profileCacheManager;
    private final ProfileRepo profileRepo;
    private final Profilemapper profilemapper;
    private final FollowRepo followRepo;
    private final BlocksRepo blocksRepo;
    private final FollowCacheWriter followCacheWriter;

    @CheckUserExistence
    public ProfileDetails getUserProfile(String targetUserId){
       String currentUserId=authenticatedUserService.getCurrentUser();
       ProfileInfo profileInfo= getUserProfileInfo(targetUserId);
        ProfileDetails profileDetails=profilemapper.toProfileDetails(profileInfo);

        profileDetails.setFollowers(followCacheWriter.UserFollowerCount(targetUserId));
        profileDetails.setFollowings(followCacheWriter.UserFollowingCount(targetUserId));
        // no relation should be set
        if(currentUserId.equals(targetUserId)){
            return profileDetails;
        }


     if(blocksRepo.existsByBlockerIdAndBlockedId(currentUserId,targetUserId)||blocksRepo.existsByBlockerIdAndBlockedId(targetUserId,currentUserId)){
         profileDetails.setBio(null);
         profileDetails.setAvatarurl(null);
         profileDetails.setUsername("Instagram User");
         return profileDetails;
     }

     RelationshipStatus relationship=resolveRelation(currentUserId,targetUserId);

      profileDetails.setRelationship(relationship);
        return profileDetails;
    }

    private RelationshipStatus resolveRelation(String currentUserId,String targetUserId){
        RelationshipStatus status=RelationshipStatus.NOT_FOLLOWING;

        if(followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId,targetUserId, Follow.Status.ACCEPTED)){
            status= RelationshipStatus.FOLLOWING;
        }else if(followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId,targetUserId, Follow.Status.PENDING)){
            status=RelationshipStatus.FOLLOW_REQUESTED;
        }else if(followRepo.existsByFollowerIdAndFollowingIdAndStatus(targetUserId,currentUserId, Follow.Status.ACCEPTED)){
            status=RelationshipStatus.FOLLOWED;
        }else if(followRepo.existsByFollowerIdAndFollowingIdAndStatus(targetUserId,currentUserId, Follow.Status.PENDING)){
            status=RelationshipStatus.FOLLOW_REQUEST_RECEIVED;
        }
        return status;
    }

    public ProfileSettingsDto getMyProfileSettings(){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Profile profile=getUserProfile(currentUserId,false);
        return profilemapper.toProfileSettingsDto(profile.getProfileSettings());
    }

    public Profile getUserProfile(String userId, Boolean cacheProfile){
        Profile profile = profileCacheManager.getProfile(userId).orElseGet(() -> profileRepo.findByUserId(userId));
        if(cacheProfile){
            profileCacheManager.cacheUserProfile(profile);
        }
        return profile;
    }


    // this method is used to fetch and cache if needed for operation that needs only the main properties of a profile {username,bio,avatar}
    public ProfileInfo getUserProfileInfo(String userId){

        return profileCacheManager.getProfileInfo(userId).orElseGet(()->{
           Profile profile=getUserProfile(userId,false);
           profileCacheManager.cacheProfileInfo(profile);
           return profilemapper.toProfileInfo(profile);
        });

    }
}
