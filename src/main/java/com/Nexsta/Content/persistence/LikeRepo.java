package com.Nexsta.Content.persistence;

import com.Nexsta.Content.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LikeRepo extends JpaRepository<Like,String> {


    Optional<Like> findByUserIdAndCommentId(String userId,String commentId);
    Optional<Like> findByUserIdAndStoryId(String userId,String storyId);
    @Query("SELECT l.comment.id FROM Like l WHERE l.user.id = :userId AND l.comment.id IN :commentIds")
    Set<String> getLikesCommentsIds(@Param("userId") String userId, @Param("commentIds") List<String> commentIds);
}
