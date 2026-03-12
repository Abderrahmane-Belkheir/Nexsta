package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepo extends JpaRepository<Post,String> {

    @Modifying
    @Transactional
    @Query("update Post p set p.postStatus = :status where " +
            "p.id = :postId and p.postStatus in :allowedStatuses and p.user.id = :userId")
    int updatePostStatus(@Param("postId") String postId, @Param("status") Post.PostStatus status,
                         @Param("userId") String userId, @Param("allowedStatuses") List<Post.PostStatus> allowedStatuses);

    Optional<Post> findByIdAndUserIdAndPostStatus(String userId, String postId, Post.PostStatus postStatus);
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId ")
    void incrementPostLikes(@Param("postId") String postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount>0")
    void decrementPostLikes(@Param("postId") String postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementPostComments(@Param("postId") String postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount=p.commentCount-1 WHERE p.id= :postId AND p.commentCount>0")
    void decrementPostComments(@Param("postId") String postId);
    

    Page<Post> findByUserIdAndPostStatus(String userId, Post.PostStatus postStatus, Pageable pageable);

}
