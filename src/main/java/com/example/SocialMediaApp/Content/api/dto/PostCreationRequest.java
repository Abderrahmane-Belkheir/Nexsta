package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Location;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import com.example.SocialMediaApp.Validation.Annotations.ValidScheduled;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@ValidScheduled
public class PostCreationRequest {

    private String caption;
    @Size(min = 1, max = 10)
    private List<String> uploadRequestsIds;
    private List<String> tags;
    private PostSettings postSettings;
    private Location location;


    private PostAction postAction;
    private Instant scheduleAt;
    public enum PostAction{DRAFT,PUBLISHED,SCHEDULED}
}
