package com.Nexsta.Profile.api.dto;

import com.Nexsta.SocialGraph.domain.RelationshipStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDetails {
    private String userId;
    private String username;
    private String avatarurl;
    private String bio;
    private RelationshipStatus relationship;
    private String followers;
    private String followings;
    private String posts;
    public ProfileDetails(String userId, RelationshipStatus relationship){
        this.userId=userId;
        this.relationship=relationship;
    }
}
