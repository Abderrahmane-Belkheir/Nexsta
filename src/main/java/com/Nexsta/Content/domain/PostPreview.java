package com.Nexsta.Content.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostPreview {
    private Media.MediaType mediaType;
    private String thumbnail;
    @Transient
    private String thumbnailFilePath;
}
