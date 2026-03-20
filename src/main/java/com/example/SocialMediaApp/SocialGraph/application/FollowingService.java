package com.example.SocialMediaApp.SocialGraph.application;

import com.example.SocialMediaApp.SocialGraph.Exceptions.BadFollowRequestException;
import com.example.SocialMediaApp.SocialGraph.Exceptions.NoRelationShipException;
import com.example.SocialMediaApp.SocialGraph.api.dto.FollowToggleResponse;
import com.example.SocialMediaApp.SocialGraph.application.cache.FollowCacheUpdater;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.Notification.domain.events.FollowNotification;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.domain.RelationshipStatus;
import com.example.SocialMediaApp.SocialGraph.domain.events.FollowAdded;
import com.example.SocialMediaApp.SocialGraph.domain.events.FollowRemoved;
import com.example.SocialMediaApp.SocialGraph.persistence.BlocksRepo;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FollowingService {

    private final FollowRepo followRepo;
    private final ProfileRepo profileRepo;
    private final BlocksRepo blocksRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final FollowCacheUpdater followCacheUpdater;

    @CheckUserExistence
    public FollowToggleResponse toggleFollow(String targetUserId){

        String currentUserId=authenticatedUserService.getCurrentUser();

        if (currentUserId.equals(targetUserId)) {
            throw new BadFollowRequestException("you cant follow yourself");
        }

        if(blocksRepo.existsByBlockerIdAndBlockedId(currentUserId,targetUserId)||blocksRepo.existsByBlockerIdAndBlockedId(targetUserId,currentUserId)) {
            throw new BadFollowRequestException("You cant follow this user");
        }

        Optional<Follow> follow=followRepo.findByFollowerIdAndFollowingId(currentUserId,targetUserId);
        FollowToggleResponse followToggleResponse=new FollowToggleResponse(targetUserId);

        if(follow.isEmpty()) {
           RelationshipStatus relationshipStatus=follow(currentUserId,targetUserId);
            followToggleResponse.setRelationshipStatus(relationshipStatus);
            return followToggleResponse;
        }
        unFollow(follow.get());
        followToggleResponse.setRelationshipStatus(RelationshipStatus.NOT_FOLLOWING);
        return followToggleResponse;

    }

    private RelationshipStatus follow(String currentUserId,String targetUserId) {

        Follow follow = new Follow(currentUserId,targetUserId);

        FollowNotification notification= new FollowNotification(currentUserId,targetUserId,
                FollowNotification.notificationType.FOLLOW);
        RelationshipStatus status;
        if (profileRepo.existsByUserIdAndProfileSettingsIsPrivateFalse(targetUserId)) {
            follow.setStatus(Follow.Status.ACCEPTED);
            follow.setFollowDate(Instant.now());
            log.info("publishing follow event for "+targetUserId);
            eventPublisher.publishEvent(notification);
            eventPublisher.publishEvent(new FollowAdded(follow));
            status=RelationshipStatus.FOLLOWING;
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS, currentUserId, FollowCacheUpdater.UpdateType.INCREMENT);
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS, targetUserId, FollowCacheUpdater.UpdateType.INCREMENT);
        } else {
            follow.setStatus(Follow.Status.PENDING);
            log.info("publishing follow request event for "+targetUserId);
            notification.setType(FollowNotification.notificationType.FOLLOW_REQUESTED);
            eventPublisher.publishEvent(notification);
            status=RelationshipStatus.FOLLOW_REQUESTED;
        }

        followRepo.save(follow);
        return status;
    }

    // this method works for both pending and accepted followings
    public void unFollow(Follow follow) {
        followRepo.delete(follow);
        if(follow.getStatus()==Follow.Status.ACCEPTED){
            eventPublisher.publishEvent(new FollowRemoved(follow));
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS,follow.getFollower_id(), FollowCacheUpdater.UpdateType.DECREMENT);
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS, follow.getFollowing_id(), FollowCacheUpdater.UpdateType.DECREMENT);
        }
    }

    // this method works for both pending and accepted followers
    public void removeFollower(String targetUserId) {
        String  currentUserId= authenticatedUserService.getCurrentUser();
        Follow follow = followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId).
                orElseThrow(()->new NoRelationShipException("No relationship with user found"));
        followRepo.delete(follow);
        if(follow.getStatus()== Follow.Status.ACCEPTED){
            eventPublisher.publishEvent(new FollowRemoved(follow));
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS,currentUserId, FollowCacheUpdater.UpdateType.DECREMENT);
            followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS, targetUserId, FollowCacheUpdater.UpdateType.DECREMENT);
        }else{
            FollowNotification notification=new FollowNotification(currentUserId,targetUserId,
                    FollowNotification.notificationType.FOLLOWING_REJECTED);
            eventPublisher.publishEvent(notification);
        }
    }

}


