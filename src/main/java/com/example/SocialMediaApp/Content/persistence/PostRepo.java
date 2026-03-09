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
    @Query(value = "UPDATE Post SET likes = likes + :delta WHERE id = :postId RETURNING likeCount",nativeQuery = true)
    long updatePostLikes(@Param("postId") String postId,@Param("delta") long delta);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Post SET commentCount = commentCount + 1 WHERE id = :postId RETURNING commentCount",nativeQuery = true)
    long incrementPostComments(@Param("postId") String postId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Post SET commentCount = commentCount - 1 WHERE Post.id=(SELECT p.id FROM Post p JOIN Comment c ON p.id=c.post_id WHERE c.id=:commentId) RETURNING commentCount",nativeQuery = true)
    long decrementPostComments(@Param("commentId") String commentId);


    Page<Post> findByUserIdAndPostStatus(String userId, Post.PostStatus postStatus, Pageable pageable);

}
