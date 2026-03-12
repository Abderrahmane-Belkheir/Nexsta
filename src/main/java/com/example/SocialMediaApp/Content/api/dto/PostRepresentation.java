package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Location;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Profile.domain.cache.ProfileInfo;
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
    private Instant publishedAt;
    private String caption;
    private Post.PostStatus postStatus;
    private Long likes;
    private boolean commentsDisabled;
    private Long comments;
    @Builder.Default
    private List<MediaRepresentation> mediaList=new ArrayList<>();
    private Location location;
    private Boolean likedByMe;
}
