package com.Nexsta.Content.api.dto;

import com.Nexsta.Content.domain.FetchDirection;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostRepresentationResponse {
    private List<PostRepresentation> previousPosts;
    private PostRepresentation currentPost;
    private List<PostRepresentation> nextPosts;
    public PostRepresentationResponse(List<PostRepresentation> postRepresentations, FetchDirection direction){
        if(direction==FetchDirection.UP) this.nextPosts=postRepresentations;
        else if (direction==FetchDirection.DOWN) this.previousPosts=postRepresentations;
    }
}
