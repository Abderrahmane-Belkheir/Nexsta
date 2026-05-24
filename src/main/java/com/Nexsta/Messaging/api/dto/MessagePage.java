package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessagePage {
   private List<MessageView> messages;
    private String oldestCursor;
    private String newestCursor;
}
