package com.Nexsta.Shared;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ServerInstance {
    @Value("${instanceId}")
    private String instanceId;
}
