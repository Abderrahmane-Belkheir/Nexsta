package com.Nexsta.SocialGraph.api.Controllers;

import com.Nexsta.SocialGraph.application.FollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowerController {

    private final FollowerService followerService;

    @PutMapping("/{userId}/accept")
    public void acceptFollow(@PathVariable String userId) {
        followerService.acceptFollow(userId);
    }

    @DeleteMapping("/{userId}/reject")
    public void rejectFollow(@PathVariable String userId) {
        followerService.rejectFollow(userId);
    }

}

