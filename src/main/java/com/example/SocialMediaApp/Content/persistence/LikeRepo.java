package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Like;
import com.example.SocialMediaApp.Content.domain.LikeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface LikeRepo extends JpaRepository<Like,String> {

    void deleteByTargetId(String targetId);
    void deleteByUserIdAndTargetIdAndType(String userId, String targetId, LikeType likeType);
    boolean existsByUserIdAndTargetIdAndType(String userId, String targetId, LikeType likeType);
    @Query("SELECT l.targetId FROM Like l WHERE l.user.id = :userId AND l.targetId IN :commentIds")
    Set<String> getLikesCommentsIds(@Param("userId") String userId, @Param("commentIds") List<String> commentIds);
}
