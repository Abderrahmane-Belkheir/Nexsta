package com.Nexsta.User.api.Controllers;

import com.Nexsta.User.application.UserActivityTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserActivityController {

    private final UserActivityTracker userActivityService;

    @MessageMapping("/heartbeat")
    public void heartbeat(Principal principal){
        log.info("heartbeat "+principal.getName());
        userActivityService.setUserActive(principal.getName());
        userActivityService.setUserLastSeen(principal.getName());
    }

}
