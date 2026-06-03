package com.Nexsta.Messaging.api.dto;


import lombok.Getter;

import java.util.List;

@Getter
public class TypingDelivery extends BaseDelivery{

    private TypingEvent event;

    public TypingDelivery(List<String> receivers,TypingEvent event){
        super(receivers);
        this.event=event;
    }

}
