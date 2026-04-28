package com.Nexsta.SocialGraph.application;

import com.Nexsta.Profile.api.dto.ProfileSummary;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.domain.RelationshipStatus;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class FollowRelationShipResolver {

    private final FollowRepo followRepo;

    public void resolveCurrentUserFollowRelationShip(List<ProfileSummary> profileSummaries, String viewerId, FollowQueryHelper.Position position, Follow.Status followStatus){
        Map<String, ProfileSummary> summaryMap = profileSummaries.stream()
                .collect(Collectors.toMap(ProfileSummary::getUserId, Function.identity()));

        List<String> targetUserIds = summaryMap.keySet().stream().toList();
        if(position== FollowQueryHelper.Position.FOLLOWERS){
            // starting from the well know relation which in this case followed if the method is used on followers list
            // or follow request received if its used to get follow requests
            // then trying to inform the user of any relation exists he doest know about
            // and sticking to the base relation if no other relation was found
            if(followStatus== Follow.Status.ACCEPTED){
                profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.FOLLOWED));
            }else{
                profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.FOLLOW_REQUEST_RECEIVED));
            }
            List<Follow> followers =followRepo.findByFollowerIdAndFollowingIdIn(viewerId,targetUserIds);
            for(Follow follow: followers){
                ProfileSummary profileSummary= summaryMap.get(follow.getFollowing_id());
                if(profileSummary!=null){
                    if(follow.getStatus()== Follow.Status.ACCEPTED){
                        profileSummary.setStatus(RelationshipStatus.FOLLOWING);
                    }else{
                        profileSummary.setStatus(RelationshipStatus.FOLLOW_REQUESTED);
                    }
                }
            }
            return;
        }

        // starting from the well know relation which in this case following if the method is used on followings list
        // or follow requested if its used to get following requests
        if(followStatus== Follow.Status.ACCEPTED){
            profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.FOLLOWING));
        }else{
            profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.FOLLOW_REQUESTED));
        }
        List<Follow> followings=followRepo.findByFollowingIdAndFollowerIdIn(viewerId,targetUserIds);
        for(Follow follow: followings){
            ProfileSummary profileSummary= summaryMap.get(follow.getFollower_id());
            if(profileSummary!=null){
                if(follow.getStatus()== Follow.Status.ACCEPTED){
                    profileSummary.setStatus(RelationshipStatus.FOLLOWED);
                }else{
                    profileSummary.setStatus(RelationshipStatus.FOLLOW_REQUEST_RECEIVED);
                }
            }
        }
    }



    public void resolveViewerFollowRelationShip(List<ProfileSummary> profileSummaries, String viewerId) {
        Map<String, ProfileSummary> summaryMap = profileSummaries.stream()
                .collect(Collectors.toMap(ProfileSummary::getUserId, Function.identity()));

        List<String> targetUserIds = summaryMap.keySet().stream().toList();
        // here there is no relation know in advanced so starting from not following
        // and then tring to resolve the relation starting from the viewer side
        profileSummaries.forEach(profileSummary -> profileSummary.setStatus(RelationshipStatus.NOT_FOLLOWING));
        List<Follow> outgoing = followRepo.findByFollowerIdAndFollowingIdIn(viewerId, targetUserIds);
        for (Follow follow : outgoing) {
            ProfileSummary summary = summaryMap.get(follow.getFollowing_id());
            if (summary != null) {
                RelationshipStatus status = follow.getStatus() == Follow.Status.PENDING
                        ? RelationshipStatus.FOLLOW_REQUESTED
                        : RelationshipStatus.FOLLOWING;
                summary.setStatus(status);
            }
        }


        List<Follow> incoming = followRepo.findByFollowingIdAndFollowerIdIn(viewerId,targetUserIds);
        for (Follow follow : incoming) {
            ProfileSummary profileSummary = summaryMap.get(follow.getFollower_id());
            if (profileSummary != null && profileSummary.getStatus() == RelationshipStatus.NOT_FOLLOWING) {
                RelationshipStatus status = follow.getStatus() == Follow.Status.PENDING
                        ? RelationshipStatus.FOLLOW_REQUEST_RECEIVED
                        : RelationshipStatus.FOLLOWED;
                profileSummary.setStatus(status);
            }
        }
        //checking if list fetched has the viewer user
        ProfileSummary profileSummary=summaryMap.get(viewerId);
        if(profileSummary!=null){
           profileSummary.setStatus(null);
    }

}
}