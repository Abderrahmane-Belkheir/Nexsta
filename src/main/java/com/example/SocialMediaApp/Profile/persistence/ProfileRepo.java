package com.example.SocialMediaApp.Profile.persistence;

import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.Profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepo extends JpaRepository<Profile, UUID> {
    Profile findByUserId(String userId);
    boolean existsByUserId(String userId);
   List<Profile> findByUserIdIn(List<String> usersIds);

    boolean existsByUserIdAndProfileSettingsIsPrivateFalse(String userId);
}
