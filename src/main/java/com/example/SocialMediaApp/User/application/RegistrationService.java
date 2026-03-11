package com.example.SocialMediaApp.User.application;

import com.example.SocialMediaApp.User.Exceptions.UserRegistrationException;
import com.example.SocialMediaApp.User.api.dto.UserRegistration;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Notification.persistence.NotificationSettingsRepo;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.Shared.Mappers.Usermapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepo userRepo;
    private final Usermapper usermapper;
    private final IdentityService identityService;



    @Transactional
    public String registerUser(UserRegistration userregistration){
        String userId=identityService.UserProvision(userregistration);
        User user=usermapper.toUser(userregistration);
        user.setId(userId);
        Profile profile=new Profile(userregistration.getUsername());
        profile.setUser(user);
        NotificationsSettings notificationsSettings=new NotificationsSettings();
        notificationsSettings.setUser(user);
        user.setProfile(profile);
        user.setNotificationsSettings(notificationsSettings);
        try{
        userRepo.saveAndFlush(user);
        }catch (Exception e){
            identityService.UserRemoval(userId);
          log.error("failed to save user in database removing it from auth server");
          throw new UserRegistrationException("registration failed!!");
        }
        return userId;
    }
}
