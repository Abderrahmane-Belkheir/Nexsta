package com.Nexsta.Messaging.api.dto;


import lombok.Getter;

import java.util.List;


@Getter
public class InboxDelivery extends BaseDelivery {

    private InboxEvent event;

    public InboxDelivery(List<String> receivers,InboxEvent event){
        super(receivers);
        this.event=event;
    }

}
