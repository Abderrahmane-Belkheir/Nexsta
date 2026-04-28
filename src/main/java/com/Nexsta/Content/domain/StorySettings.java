package com.Nexsta.Content.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StorySettings {

    private boolean hideViewCount;
    private StoryAudience audience;

    public enum StoryAudience{
        EVERYONE,CLOSE_FRIENDS,FOLLOWERS
    }

}
