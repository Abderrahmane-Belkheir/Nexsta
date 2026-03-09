package com.example.SocialMediaApp.Profile.application.cache;

import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import com.example.SocialMediaApp.Profile.persistence.ProfileCacheRepo;
import com.example.SocialMediaApp.Profile.persistence.ProfileInfoCacheRepo;
import com.example.SocialMediaApp.Shared.Mappers.Profilemapper;
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


    public ProfileInfo cacheProfileInfo(Profile profile){
        ProfileInfo profileInfoCache=profilemapper.toProfileInfo(profile);
        profileInfoCache.setUserId(profile.getUserId());
        return profileInfoCacheRepo.save(profileInfoCache);
    }

    public void cacheUserProfile(Profile profile){
        com.example.SocialMediaApp.Profile.domain.cache.Profile profileCache=profilemapper.toProfileCache(profile);
        profileCache.setUserId(profile.getUserId());
        profileCacheRepo.save(profileCache);
    }

    public Optional<Profile> getProfile(String userId){
        return profileCacheRepo.findByUserId(userId).map(profileCache -> {
            Profile profile=profilemapper.toProfile(profileCache);
            profile.setUser(new User(profileCache.getUserId()));
            return profile;
        });
    }

    public Optional<ProfileInfo>  getProfileInfo(String userId){
        return profileInfoCacheRepo.findById(userId);
    }

}
