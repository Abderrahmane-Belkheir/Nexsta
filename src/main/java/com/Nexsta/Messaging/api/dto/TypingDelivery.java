package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@AllArgsConstructor
@Getter
public class TypingDelivery extends BaseDelivery{
    private List<String> receivers;
    private TypingEvent event;
}
