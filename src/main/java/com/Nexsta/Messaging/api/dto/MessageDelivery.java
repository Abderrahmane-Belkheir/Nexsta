package com.Nexsta.Messaging.api.dto;

import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@AllArgsConstructor
@Getter
public class MessageDelivery extends BaseDelivery {
    private final List<String> receiversId;
    private final MessageView message;

    @Override
    public String toString() {
        return "MessageDelivery{" +
                "receiversId=" + receiversId +
                ", message=" + message +
                '}';
    }
}
