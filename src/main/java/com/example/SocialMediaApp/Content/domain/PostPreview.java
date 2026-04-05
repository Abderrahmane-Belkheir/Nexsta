package com.example.SocialMediaApp.Content.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class PostPreview {
    private Media.MediaType mediaType;
    private String thumbnailUrl;
}
