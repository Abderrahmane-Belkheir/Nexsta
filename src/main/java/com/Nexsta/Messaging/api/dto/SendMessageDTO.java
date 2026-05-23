package com.Nexsta.Messaging.api.dto;

import com.Nexsta.Validation.Annotations.ValidMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidMessage
public class SendMessageDTO {

    private String chatId;
    private String recipientId;
    @NotBlank
    private String content;
}
