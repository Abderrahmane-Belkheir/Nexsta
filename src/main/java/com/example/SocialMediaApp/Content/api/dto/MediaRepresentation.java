package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Media;
import lombok.Data;

@Data
public class MediaRepresentation {
    private String id;
    private String filepath;
    private Media.MediaType mediaType;
}
