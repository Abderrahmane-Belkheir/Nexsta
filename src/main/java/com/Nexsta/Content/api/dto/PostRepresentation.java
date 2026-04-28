package com.Nexsta.Content.api.dto;

import com.Nexsta.Content.domain.Location;
import com.Nexsta.Content.domain.Post;
import com.Nexsta.Profile.domain.cache.ProfileInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostRepresentation {
    @JsonProperty("AuthorProfile")
    private ProfileInfo profileInfo;
    private String id;
    private Instant createdAt;
    private Instant publishedAt;
    private String caption;
    private Post.PostStatus postStatus;
    private Long likes;
    private Long comments;
    private boolean commentsDisabled;
    // this will appear only when the owner is fetching post
    private Boolean restored;
    // only when fetching deleted post so user can know the post status if he wants to restore it
    private Post.PostStatus preDeletionPostStatus;
    // only if post is scheduled
    private Instant scheduledAt;
    @Builder.Default
    private List<MediaRepresentation> mediaList=new ArrayList<>();
    private Location location;
    private Boolean likedByMe;
}
