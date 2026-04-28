package com.Nexsta.Content.api.dto;

import com.Nexsta.Profile.api.dto.ProfileSummary;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentRepresentation {
    @JsonProperty("AuthorProfile")
    private ProfileSummary profileSummary;
    private String id;
    private String content;
    private Instant createdAt;
    private long likeCount;
    // Null if current is a reply
    private Long replyCount;
    private boolean likedByMe;
}
