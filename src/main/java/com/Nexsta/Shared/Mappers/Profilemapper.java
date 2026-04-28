package com.Nexsta.Shared.Mappers;

import com.Nexsta.Notification.api.dto.NotificationSettings;
import com.Nexsta.Profile.domain.cache.Profile;
import com.Nexsta.Notification.domain.NotificationsSettings;
import com.Nexsta.Profile.api.dto.ProfileDetails;
import com.Nexsta.Profile.api.dto.ProfileSettingsDto;
import com.Nexsta.Profile.domain.ProfileSettings;
import com.Nexsta.Profile.domain.cache.ProfileInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Profilemapper {
   ProfileDetails toProfileDetails(ProfileInfo profileInfo);

   // profileSummary toprofileSummary(ProfileInfo profileInfo);


    com.Nexsta.Profile.domain.Profile toProfile(Profile profile);

    Profile toProfileCache(com.Nexsta.Profile.domain.Profile profile);

    ProfileSettingsDto toProfileSettingsDto(ProfileSettings profileSettings);
    ProfileSettings toProfileSettings(ProfileSettingsDto profileSettingsDto);


    ProfileInfo toProfileInfo(com.Nexsta.Profile.domain.Profile profile);

    NotificationSettings toNotificationSettings(NotificationsSettings notificationsSettings);
}
