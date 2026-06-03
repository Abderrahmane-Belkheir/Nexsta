package com.Nexsta.Messaging.api.dto;



import lombok.Getter;

import java.util.List;


@Getter
public class MessageDelivery extends BaseDelivery {

    private  MessageView message;

    public MessageDelivery(List<String> receivers,MessageView message){
        super(receivers);
        this.message=message;
    }
}
