package com.example.SocialMediaApp.SocialGraph.api.Controllers;

import com.example.SocialMediaApp.SocialGraph.application.FollowRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowRequestsController {

    private final FollowRequestService followRequestService;


    @PutMapping("/{userId}/accept")
    public void acceptFollow(@PathVariable String userId) {
        followRequestService.acceptFollow(userId);
    }

    @DeleteMapping("/{userId}/reject")
    public void rejectFollow(@PathVariable String userId) {
        followRequestService.rejectFollow(userId);
    }

    @DeleteMapping("/{userId}/unsend")
    public void unsendRequest(@PathVariable String userId) {
        followRequestService.unsendFollowingRequest(userId);
    }
}

