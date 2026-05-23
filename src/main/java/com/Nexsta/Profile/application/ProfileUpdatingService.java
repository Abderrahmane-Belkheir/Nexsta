package com.Nexsta.Profile.application;

import com.Nexsta.Profile.api.dto.ProfileSettingsDto;
import com.Nexsta.Profile.domain.ProfileSettings;
import com.Nexsta.Shared.Mappers.Profilemapper;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import com.Nexsta.Storage.StorageService;
import com.Nexsta.Upload.application.UploadGatewayService;
import com.Nexsta.User.application.AuthenticatedUserService;
import com.Nexsta.User.application.IdentityService;
import com.Nexsta.User.domain.User;
import com.Nexsta.User.persistence.UserRepo;
import com.Nexsta.Profile.api.dto.Profile;
import com.Nexsta.Profile.application.cache.ProfileCacheManager;
import com.Nexsta.Profile.persistence.ProfileRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileUpdatingService {

    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileRepo profileRepo;
    private final ProfileCacheManager profileCacheManager;
    private final UserRepo userRepo;
    private final ProfileQueryService profileQueryService;
    private final UploadGatewayService uploadGatewayService;
    private final StorageService storageService;
    private final FollowRepo followRepo;
    private final IdentityService identityService;
    private final Profilemapper profilemapper;

    public void UpdateProfileSettings(ProfileSettingsDto profileSettingsDto){
        String currentUserId= authenticatedUserService.getCurrentUser();
        com.Nexsta.Profile.domain.Profile currentprofile= profileQueryService.getUserProfile(currentUserId,false);
        ProfileSettings profileSettings=currentprofile.getProfileSettings();
        boolean preStatus=profileSettings.isPrivate();

        currentprofile.setProfileSettings(profilemapper.toProfileSettings(profileSettingsDto));
        profileRepo.save(currentprofile);
        profileCacheManager.cacheUserProfile(currentprofile);
        // if profile was set from private to public all follow request to this user must be deleted
        if(preStatus&&!profileSettingsDto.isPrivate()){
            followRepo.deleteByFollowingIdAndStatus(currentUserId, Follow.Status.PENDING);
        }
    }

    public void changeProfileAvatar(MultipartFile file) throws IOException {
        String currentUserId= authenticatedUserService.getCurrentUser();

        com.Nexsta.Profile.domain.Profile currentprofile= profileQueryService.getUserProfile(currentUserId,false);

        String oldAvatarUri=currentprofile.getAvatarPath();

        String profileAvatarUri=null;


        if(oldAvatarUri!=null){
                //storageService.deleteFile(oldAvatarUri);
        }

        currentprofile.setAvatarPath(profileAvatarUri);

        profileRepo.save(currentprofile);
        // in case of cache still valid to update it to avoid any inconsistencies
        profileCacheManager.cacheUserProfile(currentprofile);
        profileCacheManager.cacheProfileInfo(currentprofile);
    }

    @Transactional
    public void UpdateProfile(Profile profile){
        String currentUserId=authenticatedUserService.getCurrentUser();
        User  currentuser= userRepo.findById(currentUserId).get();
        com.Nexsta.Profile.domain.Profile currentprofile= profileQueryService.getUserProfile(currentuser.getId(),false);
        currentprofile.setUsername(profile.getUsername());
        currentprofile.setBio(profile.getBio());
        currentuser.setUserName(profile.getUsername());
        currentprofile.setUsername(profile.getUsername());
        currentuser.setProfile(currentprofile);
        userRepo.save(currentuser);
        if(!currentprofile.getUsername().equals(profile.getUsername())){
            identityService.changeUsername(currentUserId, profile.getUsername());
        }
        profileCacheManager.cacheUserProfile(currentprofile);
        profileCacheManager.cacheProfileInfo(currentprofile);
    }
}
