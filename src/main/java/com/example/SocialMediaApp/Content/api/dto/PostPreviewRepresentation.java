package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.PostPreview;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostPreviewRepresentation {
    private String id;
    private PostPreview postPreview;
    private long likes;
    private long comments;
}
