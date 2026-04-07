package com.example.SocialMediaApp.Content.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostPreview {
    private Media.MediaType mediaType;
    private String thumbnail;
}
