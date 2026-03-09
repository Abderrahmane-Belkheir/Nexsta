package com.example.SocialMediaApp.Messaging.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDTO {
    private String id;
    private String content;
    private Instant sentAt;
    private Boolean lastView;
    private Boolean sentByme;
}
