package com.example.SocialMediaApp.SocialGraph.api.dto;

import com.example.SocialMediaApp.SocialGraph.domain.RelationshipStatus;
import lombok.AllArgsConstructor;
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
