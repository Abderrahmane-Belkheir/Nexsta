package com.example.SocialMediaApp.Content.persistence;

import com.example.SocialMediaApp.Content.domain.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment,String> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE Comment c SET c.likeCount = c.likeCount + :delta WHERE c.id = :commentId")
    void updateCommentLikes(@Param("commentId") String commentId, @Param("delta") int delta);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Comment c SET c.replyCount = c.replyCount + :delta WHERE c.id = :commentId")
    void updateCommentReplies(@Param("commentId") String commentId, @Param("delta") int delta);

    int countByParentComment(Comment comment);



}
