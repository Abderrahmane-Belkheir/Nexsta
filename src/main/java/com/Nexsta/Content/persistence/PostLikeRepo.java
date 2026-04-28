package com.Nexsta.Content.persistence;

import com.Nexsta.Content.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostLikeRepo extends JpaRepository<PostLike,String> {
    Optional<PostLike> findByUserIdAndPostId(String userId,String postId);
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId AND pl.post.id IN :postIds")
    Set<String> getLikesPostIds(@Param("userId") String userId,@Param("postIds") List<String> postIds);

}
