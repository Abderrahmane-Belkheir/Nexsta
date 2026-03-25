package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Location;
import com.example.SocialMediaApp.Content.domain.PostSettings;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class PostCreationRequest {
    private String caption;
    @Size(min = 1, max = 10)
    private List<String> uploadRequestsIds;
    private List<String> tags;
    private PostSettings postSettings;
    private Location location;
}
