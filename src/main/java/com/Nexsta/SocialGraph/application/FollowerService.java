package com.Nexsta.SocialGraph.application;

import com.Nexsta.Notification.domain.events.FollowNotification;
import com.Nexsta.SocialGraph.Exceptions.BadFollowRequestException;
import com.Nexsta.SocialGraph.Exceptions.NoRelationShipException;
import com.Nexsta.SocialGraph.application.cache.FollowCache;
import com.Nexsta.User.application.AuthenticatedUserService;
import com.Nexsta.Shared.CheckUserExistence;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
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
    private final FollowCache followCacheUpdater;

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
        eventPublisher.publishEvent(new FollowNotification(currentUserId,targetUserId,
                FollowNotification.notificationType.FOLLOWING_ACCEPTED));
        followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWERS,currentUserId, FollowCache.UpdateType.INCREMENT);
        followCacheUpdater.UpdateCount(FollowQueryHelper.Position.FOLLOWINGS,targetUserId, FollowCache.UpdateType.INCREMENT);
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
        eventPublisher.publishEvent(new FollowNotification(currentUserId,targetUserId,
                FollowNotification.notificationType.FOLLOWING_REJECTED));
    }
}
