package com.Nexsta.Upload.api.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
public class UnsupportedMediaTypeResponse {
    private String message;
    private List<String> supportedMediaTypes;
}
