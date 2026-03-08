package com.example.SocialMediaApp.Profile.api.dto;

import com.example.SocialMediaApp.SocialGraph.domain.RelationshipStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileSummary {
    private String userId;
    private String username;
    private String avatarurl;
    private RelationshipStatus status;
    public ProfileSummary(String userId){
        this.userId=userId;
    }
}
