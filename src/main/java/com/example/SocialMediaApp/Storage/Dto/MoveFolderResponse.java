package com.example.SocialMediaApp.Storage.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
public class MoveFolderResponse {
    private Boolean success;
    private List<String> moved;
    private List<MoveError> errors;
    private String error;
}
