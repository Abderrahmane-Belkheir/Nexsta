package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Notification.api.dto.NotificationSettings;
import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Profile.api.dto.ProfileDetails;
import com.example.SocialMediaApp.Profile.api.dto.ProfileSettingsDto;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.domain.ProfileSettings;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Profilemapper {
   ProfileDetails toProfileDetails(ProfileInfo profileInfo);

   // profileSummary toprofileSummary(ProfileInfo profileInfo);


    Profile toProfile(com.example.SocialMediaApp.Profile.domain.cache.Profile profile);

    com.example.SocialMediaApp.Profile.domain.cache.Profile toProfileCache(Profile profile);

    ProfileSettingsDto toProfileSettingsDto(ProfileSettings profileSettings);
    ProfileSettings toProfileSettings(ProfileSettingsDto profileSettingsDto);


    ProfileInfo toProfileInfo(Profile profile);

    NotificationSettings toNotificationSettings(NotificationsSettings notificationsSettings);
}
