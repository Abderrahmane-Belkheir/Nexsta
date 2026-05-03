package com.Nexsta.User.persistence;

import com.Nexsta.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface UserRepo extends JpaRepository<User,String> {
    boolean existsUserByUserName(String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.followerCount = u.followerCount + :delta WHERE u.id= :userId")
    void updateUserFollowers(@Param("userId")String userId,@Param("delta")int delta);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.followingCount = u.followingCount + :delta WHERE u.id= :userId")
    void updateUserFollowings(@Param("userId")String userId,@Param("delta")int delta);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.postCount = u.postCount + :delta WHERE u.id= :userId")
    void updateUserPosts(@Param("userId")String userId,@Param("delta")int delta);

    Optional<User> findByUserName(String username);
}
