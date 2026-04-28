package com.Nexsta.Upload.api.dto;

import com.Nexsta.Upload.domain.UploadType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UploadRequest {
    private UploadType uploadType;
    private String fileMimeType;
    private Long fileSize;
    @Max(15)
    @Min(1)
    private Integer duration;
}
