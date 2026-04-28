package com.Nexsta.Content.api.dto;

import com.Nexsta.Content.domain.Post;
import com.Nexsta.Content.domain.PostSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostUpdateResponse {
    private String id;
    private Instant createdAt;
    private Post.PostStatus postStatus;
    private String caption;
    private String thumbnailUrl;
    private PostSettings postSettings;
    private List<MediaRepresentation> mediaRepresentationList;
}
