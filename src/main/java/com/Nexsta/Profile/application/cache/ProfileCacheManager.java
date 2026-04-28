package com.Nexsta.Profile.application.cache;

import com.Nexsta.Profile.domain.cache.Profile;
import com.Nexsta.User.domain.User;
import com.Nexsta.Profile.domain.cache.ProfileInfo;
import com.Nexsta.Profile.persistence.ProfileCacheRepo;
import com.Nexsta.Profile.persistence.ProfileInfoCacheRepo;
import com.Nexsta.Shared.Mappers.Profilemapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileCacheManager {

    private final ProfileCacheRepo profileCacheRepo;
    private final Profilemapper profilemapper;
    private final ProfileInfoCacheRepo profileInfoCacheRepo;


    public ProfileInfo cacheProfileInfo(com.Nexsta.Profile.domain.Profile profile){
        ProfileInfo profileInfoCache=profilemapper.toProfileInfo(profile);
        profileInfoCache.setUserId(profile.getUserId());
        return profileInfoCacheRepo.save(profileInfoCache);
    }

    public void cacheUserProfile(com.Nexsta.Profile.domain.Profile profile){
        Profile profileCache=profilemapper.toProfileCache(profile);
        profileCache.setUserId(profile.getUserId());
        profileCacheRepo.save(profileCache);
    }

    public Optional<com.Nexsta.Profile.domain.Profile> getProfile(String userId){
        return profileCacheRepo.findByUserId(userId).map(profileCache -> {
            com.Nexsta.Profile.domain.Profile profile=profilemapper.toProfile(profileCache);
            profile.setUser(new User(profileCache.getUserId()));
            return profile;
        });
    }

    public Optional<ProfileInfo>  getProfileInfo(String userId){
        return profileInfoCacheRepo.findById(userId);
    }

}
