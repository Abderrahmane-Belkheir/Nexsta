package com.Nexsta.Notification.api.dto;

import lombok.Data;

@Data
public class EmailSendingResponse {
    private String messageId;
    private String batchId;
}
