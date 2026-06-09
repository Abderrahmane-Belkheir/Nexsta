
package com.Nexsta.SocialGraph.api.Controllers;

import com.Nexsta.Profile.api.dto.ProfileSummary;
import com.Nexsta.SocialGraph.api.dto.FollowQueryResponse;
import com.Nexsta.SocialGraph.application.FollowQueryService;
import com.Nexsta.SocialGraph.domain.Follow;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class FollowQueryController {
    private final FollowQueryService followQueryService;

    @GetMapping("/{userid}/followers")
    public ResponseEntity<FollowQueryResponse> getUserFollowers(@PathVariable String userid,@RequestParam String cursor) {
        return ResponseEntity.ok(followQueryService.listUserFollowers(userid, cursor));
    }

    @GetMapping("/{userid}/followings")
    public ResponseEntity<FollowQueryResponse> getUserFollowings(@PathVariable String userid, @RequestParam String cursor) {
        return ResponseEntity.ok(followQueryService.listUserFollowing(userid, cursor));
    }

    @GetMapping("/me/followers")
    public ResponseEntity<FollowQueryResponse> getMyFollowers(@RequestParam(defaultValue = "ACCEPTED") Follow.Status status,@RequestParam String cursor) {

        return ResponseEntity.ok(status==Follow.Status.ACCEPTED?
                followQueryService.listCurrentUserFollowers(cursor):followQueryService.listCurrentUserFollowRequests(cursor));
    }

    @GetMapping("/me/followings")
    public ResponseEntity<FollowQueryResponse> getMyFollowings(@RequestParam(defaultValue = "ACCEPTED") Follow.Status status,@RequestParam String cursor) {

        return ResponseEntity.ok(status==Follow.Status.ACCEPTED?
                followQueryService.listCurrentUserFollowings(cursor):followQueryService.listCurrentUserFollowingRequests(cursor));
    }

}

