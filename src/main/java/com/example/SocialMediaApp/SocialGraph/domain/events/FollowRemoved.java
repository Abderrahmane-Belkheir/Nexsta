package com.example.SocialMediaApp.SocialGraph.domain.events;

import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowRemoved {
    private Follow follow;
}
