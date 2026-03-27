package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MediaRepo extends JpaRepository<Media,String>{
    @Query("SELECT DISTINCT m FROM Media m WHERE m.post.id IN:postIds")
    List<Media> findByPostIdIn(@Param("postIds") List<String> postIds);
    List<Media>  findByStoryIn(List<String> storyIds);
}
