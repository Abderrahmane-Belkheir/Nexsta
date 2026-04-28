package com.Nexsta.Upload.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UploadFailedResponse {
    private String message;
    private List<String> failedUploadIds;
}
