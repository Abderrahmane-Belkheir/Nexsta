package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Notification.api.dto.NotificationSettings;
import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Profile.api.dto.ProfileDetails;
import com.example.SocialMediaApp.Profile.api.dto.ProfileSettings;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Profilemapper {
   ProfileDetails toprofileDetails(ProfileInfo profileInfo);

   // profileSummary toprofileSummary(ProfileInfo profileInfo);


    Profile toprofile(com.example.SocialMediaApp.Profile.domain.cache.Profile profile);

    com.example.SocialMediaApp.Profile.domain.cache.Profile toprofileCache(Profile profile);

    ProfileSettings toprofilesettings(Profile profile);

    @Mapping(target = "avatarurl", source = "publicavatarurl")
    ProfileInfo toprofileInfo(Profile profile);
    NotificationSettings tonotificationsettings(NotificationsSettings notificationsSettings);
}
