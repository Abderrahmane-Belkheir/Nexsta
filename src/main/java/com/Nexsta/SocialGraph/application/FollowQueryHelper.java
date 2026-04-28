package com.Nexsta.SocialGraph.application;

import com.Nexsta.Profile.application.ProfileSummaryBuilder;
import com.Nexsta.Profile.api.dto.ProfileSummary;
import com.Nexsta.SocialGraph.application.cache.FollowCacheWriter;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowQueryHelper {

    private final FollowCacheWriter followCacheWriter;
    private final FollowRepo followRepo;
    private final FollowRelationShipResolver followRelationShipResolver;
    private final ProfileSummaryBuilder profileSummaryService;
    public enum Position {FOLLOWERS, FOLLOWINGS}

    // no cache is supposed to be here since its  viewed  by the owing user only so fetching directly from db
    public List<ProfileSummary> listCurrentUserPendingFollows(String currentUserId, Position position, int page){
        Pageable pageable= PageRequest.of(page,10);
        Page<Follow> pendingFollowsPage=position==Position.FOLLOWERS?
                followRepo.findByFollowingIdAndStatus(currentUserId, Follow.Status.PENDING,pageable):
                followRepo.findByFollowerIdAndStatus(currentUserId, Follow.Status.PENDING,pageable);
        List<Follow> pendingFollows=pendingFollowsPage.getContent();
        List<String> followsIds=pendingFollows.stream().
                map(follow -> position==Position.FOLLOWERS?follow.getFollower_id():follow.getFollowing_id()).toList();
       List<ProfileSummary> profileSummaries= profileSummaryService.buildProfileSummaries(followsIds);
        followRelationShipResolver.
                resolveCurrentUserFollowRelationShip(profileSummaries, currentUserId,position, Follow.Status.PENDING);
        return profileSummaries;
    }

    public List<ProfileSummary> listCurrentUserFollows(String userId, Position position, int page){
     Set<String> followsIds= followCacheWriter.cacheWindow(userId,page,position);
     List<ProfileSummary> profileSummaries= profileSummaryService.buildProfileSummaries(followsIds.stream().toList());
     followRelationShipResolver.
             resolveCurrentUserFollowRelationShip(profileSummaries,userId,position, Follow.Status.ACCEPTED);
     return profileSummaries;
       }

       public List<ProfileSummary> listUserFollows(String viewerId, String targetedId, Position position, int page){
           Set<String> followsIds= followCacheWriter.cacheWindow(targetedId,page,position);
          List<ProfileSummary> profileSummaries=  profileSummaryService.buildProfileSummaries(followsIds.stream().toList());
          followRelationShipResolver.
                  resolveViewerFollowRelationShip(profileSummaries,viewerId);
            return profileSummaries;
       }

}


