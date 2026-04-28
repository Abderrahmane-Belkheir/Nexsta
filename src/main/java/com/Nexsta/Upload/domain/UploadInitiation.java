package com.Nexsta.Upload.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UploadInitiation {
    private String filepath;
    private String uploadRequestId;
}
