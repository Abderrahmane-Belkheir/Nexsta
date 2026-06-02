package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class InboxDelivery extends BaseDelivery {
    private List<String> receiversId;
    private InboxEvent event;

}
