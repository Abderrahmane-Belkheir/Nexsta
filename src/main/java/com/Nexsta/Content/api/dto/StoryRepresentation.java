package com.Nexsta.Content.api.dto;

import com.Nexsta.Content.domain.Story;
import com.Nexsta.Profile.domain.cache.ProfileInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoryRepresentation {
    @JsonProperty("AuthorProfile")
    private ProfileInfo profileInfo;
    private String id;
    private Instant publishedAt;
    private Story.StoryStatus storyStatus;
    private List<MediaRepresentation> mediaList;
    private boolean seenByMe;
}
