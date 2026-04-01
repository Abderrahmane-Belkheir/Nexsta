package com.example.SocialMediaApp.Content.api.dto;

import com.example.SocialMediaApp.Content.domain.Media;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaRepresentation {
    private String id;
    private String filepath;
    private Media.MediaType mediaType;
    public MediaRepresentation(String id,Media.MediaType mediaType){
        this.id=id;
        this.mediaType=mediaType;
    }
}
