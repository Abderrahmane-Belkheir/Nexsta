package com.Nexsta.Content.api.dto;

import com.Nexsta.Content.domain.Post;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
@Data
public class PostPreviewRequest {
    @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant cursor;
    private Post.PostStatus status;
}
