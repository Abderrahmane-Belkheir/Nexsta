package com.example.SocialMediaApp.Messaging.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageToChatDTO {
    @NotBlank
    private String chatId;
    @NotBlank
    private String content;
}
