package com.Nexsta.Content.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

@Data
public class CommentCreationRequest {
    @NotBlank
    @Size(max = 500)
    private String content;
}
