package com.Nexsta.Messaging.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageView {
    private String id;
    private String senderId;
    private String content;
    private Instant sentAt;
    private boolean mine;
    private List<String> seenByUserIds;
}
