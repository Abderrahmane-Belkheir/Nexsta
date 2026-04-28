package com.Nexsta.Profile.persistence;

import com.Nexsta.Profile.domain.cache.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProfileCacheRepo extends CrudRepository<Profile,String> {
    Optional<Profile> findByUserId(String userId);
}
