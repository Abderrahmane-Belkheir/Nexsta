package com.example.SocialMediaApp.Profile.application;

import com.example.SocialMediaApp.Profile.api.dto.ProfileSummary;
import com.example.SocialMediaApp.Profile.application.cache.ProfileCacheManager;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileSummaryBuilder {
    private final ProfileCacheManager profileCacheManager;
    private final ProfileRepo profileRepo;
    private final MediaUrlResolver mediaUrlResolver;

    public List<ProfileSummary> buildProfileSummaries(List<String> usersIds){
        List<ProfileSummary> profileSummaries=usersIds.stream().map(ProfileSummary::new).toList();
        Map<String, ProfileSummary> summaryMap = profileSummaries.stream()
                .collect(Collectors.toMap(ProfileSummary::getUserId, Function.identity()));
        // fetching from cache first
        usersIds.forEach(userId->{
            Optional<ProfileInfo> profileInfo= profileCacheManager.getProfileInfo(userId);
            if(profileInfo.isPresent()){
                ProfileSummary profileSummary=summaryMap.get(userId);
                ProfileInfo profileInfo1 = profileInfo.get();
                profileSummary.setAvatarurl(mediaUrlResolver.resolveUrl(profileInfo1.getAvatarPath()));
                profileSummary.setUsername(profileInfo1.getUsername());
            }
        });
        // filtering the non fetched profile summaries to fetch them from db and cache them also
        usersIds=summaryMap.entrySet().stream().
                filter(e->e.getValue().getUsername()==null).map(Map.Entry::getKey).toList();
        List<Profile> profiles= profileRepo.findByUserIdIn(usersIds);
        for(Profile profile:profiles){
            ProfileSummary profileSummary=summaryMap.get(profile.getUserId());
            profileSummary.setUsername(profile.getUsername());
            profileSummary.setAvatarurl(profile.getAvatarPath());
            profileCacheManager.cacheProfileInfo(profile);
        }

        return profileSummaries;
    }

}
