package com.Nexsta.Content.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostPreviewResponse {
    private List<PostPreviewRepresentation> previewRepresentationList;
    private boolean hasMore;
    private Instant nextCursor;
}
