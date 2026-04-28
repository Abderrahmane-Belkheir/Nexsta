package com.Nexsta.Content.api.dto;

import com.Nexsta.Content.domain.PostPreview;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostPreviewRepresentation {
    private String id;
    private PostPreview postPreview;
    private Long likes;
    private Long comments;
}
