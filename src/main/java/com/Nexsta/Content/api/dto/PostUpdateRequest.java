package com.Nexsta.Content.api.dto;

import com.Nexsta.Content.domain.PostSettings;
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
