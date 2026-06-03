package com.Nexsta.Messaging.api.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class MessageRemovedDelivery extends BaseDelivery{

    private String messageId;

    public MessageRemovedDelivery(List<String> receivers, String messageId){
        super(receivers);
        this.messageId=messageId;
    }
}
