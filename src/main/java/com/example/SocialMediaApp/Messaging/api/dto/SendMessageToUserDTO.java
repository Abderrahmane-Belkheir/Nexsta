package com.example.SocialMediaApp.Messaging.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageToUserDTO {
    @NotBlank
    private String userId;
    @NotBlank
    private String content;

}
