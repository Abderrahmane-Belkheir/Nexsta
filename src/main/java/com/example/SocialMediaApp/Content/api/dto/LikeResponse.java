package com.example.SocialMediaApp.Content.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class LikeResponse {
    private boolean liked;
    private Long likeCount;

    public LikeResponse(boolean liked){
        this.liked=liked;
    }

}
