package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.PostSettings;
import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {
    private String id;
    private String caption;
    private List<String> mediaIds;
    private String thumbnail;
    private PostSettings postSettings;
}
