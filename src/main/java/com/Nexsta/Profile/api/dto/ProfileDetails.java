package com.Nexsta.Profile.api.dto;

import com.Nexsta.SocialGraph.domain.RelationshipStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDetails {
    private String userId;
    private String username;
    private String avatarurl;
    private String bio;
    private RelationshipStatus relationship;
    private Long followers;
    private Long followings;
    private String posts;

}
