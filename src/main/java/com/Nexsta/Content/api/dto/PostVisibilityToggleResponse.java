package com.Nexsta.Content.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostVisibilityToggleResponse {

    private  PostStatus status;

    public enum PostStatus{PUBLISHED,UNPUBLISHED}
}
