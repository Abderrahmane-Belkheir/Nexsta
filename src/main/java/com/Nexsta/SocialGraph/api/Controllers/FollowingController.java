package com.Nexsta.SocialGraph.api.Controllers;

import com.Nexsta.SocialGraph.api.dto.FollowToggleResponse;
import com.Nexsta.SocialGraph.application.FollowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;

    @PostMapping("/{userId}/toggle-follow")
    public ResponseEntity<FollowToggleResponse> toggleFollow(@PathVariable String userId){
        return ResponseEntity.ok(followingService.toggleFollow(userId));
    }

    @DeleteMapping("/{userId}/followers")
    public ResponseEntity<Void> removeFollower(@PathVariable String userId) {
        followingService.removeFollower(userId);
        return ResponseEntity.noContent().build();
    }

}

