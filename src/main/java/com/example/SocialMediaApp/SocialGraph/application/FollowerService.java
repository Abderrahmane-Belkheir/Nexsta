package com.example.SocialMediaApp.SocialGraph.application;

import com.example.SocialMediaApp.Notification.domain.events.FollowNotification;
import com.example.SocialMediaApp.SocialGraph.Exceptions.BadFollowRequestException;
import com.example.SocialMediaApp.SocialGraph.Exceptions.NoRelationShipException;
import com.example.SocialMediaApp.SocialGraph.application.cache.FollowCacheUpdater;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.domain.events.FollowAdded;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowerService {

    private final FollowRepo followRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final FollowCacheUpdater followCacheUpdater;

    @CheckUserExistence
    public void acceptFollow(String targetUserId) {
        String  currentUserId =authenticatedUserService.getCurrentUser();

        Follow followRequest = followRepo.
                findByFollowerIdAndFollowingId(targetUserId,currentUserId).
                orElseThrow(()->new NoRelationShipException("No relation with user found"));

        if(followRequest.getStatus()== Follow.Status.ACCEPTED){
            return;
        }

        followRequest.setStatus(Follow.Status.ACCEPTED);
        followRequest.setFollowDate(Instant.now());
        followRepo.save(followRequest);
        log.info("publishing following accepted event to "+targetUserId);
        eventPublisher.publishEvent(new FollowNotification(currentUserId,targetUserId,
                FollowNotification.notificationType.FOLLOWING_ACCEPTED));
        eventPublisher.publishEvent(new FollowAdded(followRequest));
        followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS,currentUserId, FollowCacheUpdater.UpdateType.INCREMENT);
        followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS,targetUserId, FollowCacheUpdater.UpdateType.INCREMENT);
    }

    @CheckUserExistence
    public void rejectFollow(String targetUserId) {
        String currentUserId = authenticatedUserService.getCurrentUser();
        Follow follow = followRepo.findByFollowerIdAndFollowingId(targetUserId, currentUserId).
                orElseThrow(()->new NoRelationShipException("No relation with user found"));
        if(follow.getStatus()== Follow.Status.ACCEPTED){
            throw new BadFollowRequestException("couldn't perform reject follow action on this user");
        }
        followRepo.delete(follow);
        log.info("publishing following rejected event to "+targetUserId);
        eventPublisher.publishEvent(new FollowNotification(currentUserId,targetUserId,
                FollowNotification.notificationType.FOLLOWING_REJECTED));
    }
}
