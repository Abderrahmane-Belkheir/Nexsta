package com.Nexsta.Profile.api.Controllers;

import com.Nexsta.Profile.api.dto.Profile;
import com.Nexsta.Profile.api.dto.ProfileDetails;
import com.Nexsta.Profile.api.dto.ProfileSettingsDto;
import com.Nexsta.Profile.application.ProfileQueryService;
import com.Nexsta.Profile.application.ProfileUpdatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileQueryService profileQueryService;
    private final ProfileUpdatingService profileUpdatingService;

    @GetMapping("/me")
    public ProfileDetails getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return profileQueryService.getUserProfile(jwt.getSubject());
    }

    @PutMapping("/me")
    public void updateProfile(@RequestBody @Valid Profile profile) {
        profileUpdatingService.UpdateProfile(profile);
    }


    @GetMapping("/{userid}")
    public ProfileDetails getProfile(@PathVariable String userid) {
        return profileQueryService.getUserProfile(userid);
    }


    @PutMapping("/me/picture")
    public void updateProfilePicture(@RequestParam MultipartFile file) throws IOException {
        profileUpdatingService.changeProfileAvatar(file);
    }

    @GetMapping("/me/settings")
    public ProfileSettingsDto getProfileSettings() {
        return profileQueryService.getMyProfileSettings();
    }

    @PutMapping("/me/settings")
    public void updateProfileSettings(@RequestBody ProfileSettingsDto settings) {
        profileUpdatingService.UpdateProfileSettings(settings);
    }
}

