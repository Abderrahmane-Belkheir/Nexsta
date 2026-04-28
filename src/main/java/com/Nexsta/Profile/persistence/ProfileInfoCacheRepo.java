package com.Nexsta.Profile.persistence;

import com.Nexsta.Profile.domain.cache.ProfileInfo;
import org.springframework.data.repository.CrudRepository;

public interface ProfileInfoCacheRepo extends CrudRepository<ProfileInfo,String> {
    ProfileInfo findByUserId(String userId);
    boolean existsByUserId(String userId);
}
