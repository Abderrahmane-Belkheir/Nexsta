package com.Nexsta.SocialGraph.api.dto;

import com.Nexsta.SocialGraph.domain.RelationshipStatus;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class FollowToggleResponse {
    private String userId;
    private RelationshipStatus relationshipStatus;
    public FollowToggleResponse(String userId){
        this.userId=userId;
    }
}
