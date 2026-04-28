package com.Nexsta.SocialGraph.domain.events;

import com.Nexsta.SocialGraph.domain.Follow;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowRemoved {
    private Follow follow;
}
