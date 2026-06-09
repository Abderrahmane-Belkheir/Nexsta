package com.Nexsta.SocialGraph.api.dto;

import com.Nexsta.Profile.api.dto.ProfileSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FollowQueryResponse {
    private List<ProfileSummary> profileSummaryList;
    private boolean hasMore;
    private String oldestCursor;
}
