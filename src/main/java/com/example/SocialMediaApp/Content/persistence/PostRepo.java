package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepo extends JpaRepository<Post,String> {

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.postStatus = :status WHERE " +
            "p.id = :postId AND p.postStatus IN :allowedStatuses AND p.user.id = :userId")
    int updatePostStatus(@Param("postId") String postId, @Param("status") Post.PostStatus status,
                         @Param("userId") String userId, @Param("allowedStatuses") List<Post.PostStatus> allowedStatuses);

    Optional<Post> findByIdAndUserIdAndPostStatus(String userId, String postId, Post.PostStatus postStatus);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.mediaList WHERE p.id= :postId AND p.user.id= :userId AND p.postStatus= 'DELETED' ")
    Optional<Post> findPostToRestore(@Param("postId") String postId, @Param("userId") String userId);
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.mediaList WHERE p.id= :postId AND p.user.id= :userId")
    Optional<Post> findByIdAndUserIdAndPostStatusWithMediaList(@Param("postId") String postId ,@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + :delta WHERE p.id = :postId")
    void updatePostLikes(@Param("postId") String postId,@Param("delta") int delta);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id= :postId")
    void updatePostComments(@Param("postId") String postId,@Param("delta") int delta);
    

    Page<Post> findByUserIdAndPostStatus(String userId, Post.PostStatus postStatus, Pageable pageable);

    Optional<Post> findByIdAndPostStatus(String postId,Post.PostStatus postStatus);

    @Modifying
    @Transactional
    @Query("DELETE FROM Post p WHERE p.postStatus=:status AND p.deletedAt < :date ")
    void deleteByOldPostsWithStatus(@Param("status") Post.PostStatus status,@Param("date") Instant date);

    Optional<Post> findByIdAndUserId(String userId,String postId);
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.postStatus='PUBLISHED',p.publishedAt=:date,p.scheduledAt=null WHERE p.id=:postId AND p.postStatus='SCHEDULED' ")
    void updateScheduledPost(@Param("postId") String postId,@Param("date") Instant date);

}
