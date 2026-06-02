package com.Nexsta.Shared;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Slf4j
public class ServerInstance {
    @Value("${instanceId}")
    private String instanceId;
    @PostConstruct
    public void init(){
        log.info("instance id is "+instanceId);
    }
}
