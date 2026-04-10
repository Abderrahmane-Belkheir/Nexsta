package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.FetchDirection;
import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepo extends JpaRepository<Post,String> {

    @Query("SELECT p FROM Post p WHERE p.user.id=:userId AND p.id=:postId LEFT JOIN FETCH p.mediaList")
    Optional<Post> findByIdAndUserIdWithMediaList(@Param("userId") String userId,@Param("postId") String postId);
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.postStatus = :status WHERE " +
            "p.id = :postId AND p.postStatus IN :allowedStatuses AND p.user.id = :userId")
    int updatePostStatus(@Param("postId") String postId, @Param("status") Post.PostStatus status,
                         @Param("userId") String userId, @Param("allowedStatuses") List<Post.PostStatus> allowedStatuses);
    @Query("SELECT p FROM Post p WHERE p.user.id=:userId AND p.postStatus=:status AND  ((:direction=UP) AND p.PublishedAt>:date) OR ((:direction=DOWN) AND  p.publishedAt<:date) ORDER By p.publishedAt DESC")
    List<Post> findPostsAboveOrBelowPost(@Param("userId") String userId, @Param("date") Instant date, @Param("status") Post.PostStatus status, @Param("direction") FetchDirection direction, Pageable pageable);
    Optional<Post> findByIdAndUserIdAndPostStatus(String postId,String userId,Post.PostStatus status);
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.mediaList WHERE p.id= :postId AND p.user.id= :userId AND p.postStatus= 'DELETED' ")
    Optional<Post> findPostToRestore(@Param("postId") String postId, @Param("userId") String userId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.mediaList WHERE p.id= :postId AND p.user.id= :userId")
    Optional<Post> findByIdAndUserIdAndPostStatusWithMediaList(@Param("postId") String postId ,@Param("userId") String userId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.mediaList WHERE p.id= :postId AND p.postStatus=:status")
    Optional<Post> findByIdAndPostStatusWithMediaList(@Param("postId") String postId ,@Param("status") Post.PostStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + :delta WHERE p.id = :postId")
    void updatePostLikes(@Param("postId") String postId,@Param("delta") int delta);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id= :postId")
    void updatePostComments(@Param("postId") String postId,@Param("delta") int delta);
    



    @Modifying
    @Transactional
    @Query("DELETE FROM Post p WHERE p.postStatus=:status AND p.deletedAt < :date ")
    void deleteByOldPostsWithStatus(@Param("status") Post.PostStatus status,@Param("date") Instant date);

    Optional<Post> findByIdAndUserId(String userId,String postId);

    List<Post> findTop10ByUserIdAndPostStatusAndPublishedAtBeforeOrderByPublishedAtDesc(String userId, Post.PostStatus status, Instant date);
    List<Post> findTop10ByUserIdAndPostStatusOrderByPublishedAtDesc(String userId,Post.PostStatus status);
    boolean existsByUserIdAndPostStatusAndPublishedAtBefore(String userId,Post.PostStatus status,Instant date);
    @Query("SELECT COUNT(p) >= :limit FROM Post p WHERE p.userId = :userId AND p.status = 'DRAFT'")
    boolean isDraftLimitReached(@Param("userId") String userId, @Param("limit") int limit);
}

