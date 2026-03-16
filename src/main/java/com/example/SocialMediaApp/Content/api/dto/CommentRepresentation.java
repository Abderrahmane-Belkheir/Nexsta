package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CommentRepresentation {
    @JsonProperty("AuthorProfile")
    private ProfileInfo profileInfo;
    private String id;
    /*
     Null for top comment (directly related to post)
     parent comment id if reply
    */
    private String parentCommentId;
    private String content;
    private Instant createdAt;
    private long likeCount;
    // Null if current is a reply
    private Long replyCount;
}
