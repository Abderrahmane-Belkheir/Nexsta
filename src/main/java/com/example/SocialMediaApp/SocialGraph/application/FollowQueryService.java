package com.example.SocialMediaApp.SocialGraph.application;

import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.SocialGraph.Exceptions.FollowListNotVisibleException;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.Profile.api.dto.ProfileSummary;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowQueryService {

    private final FollowQueryHelper followQueryHelper;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;

    public List<ProfileSummary> listCurrentUserFollowers(int page) {
        String currentUserId =authenticatedUserService.getCurrentUser();
        return  followQueryHelper.listCurrentUserFollows(currentUserId, FollowQueryHelper.Position.FOLLOWERS,page);
    }

    public List<ProfileSummary> listCurrentUserFollowings(int page) {
       String currentUserId = authenticatedUserService.getCurrentUser();
        return  followQueryHelper.listCurrentUserFollows(currentUserId, FollowQueryHelper.Position.FOLLOWINGS,page);
    }

    public List<ProfileSummary> listCurrentUserFollowRequests(int page) {
        String currentUserId= authenticatedUserService.getCurrentUser();
        return followQueryHelper.
                listCurrentUserPendingFollows(currentUserId,FollowQueryHelper.Position.FOLLOWERS,page);
    }

    public List<ProfileSummary> listCurrentUserFollowingRequests(int page) {
        String currentUserId= authenticatedUserService.getCurrentUser();
        return followQueryHelper.
                listCurrentUserPendingFollows(currentUserId, FollowQueryHelper.Position.FOLLOWINGS,page);

    }

    @CheckUserExistence
    public List<ProfileSummary> listUserFollowers(String targetUserId, int page){
        String currentUserId= authenticatedUserService.getCurrentUser();
       boolean isAllowed= visibilityPolicy.isAllowed(currentUserId, targetUserId);
       if(!isAllowed){
           throw new FollowListNotVisibleException("followers list for user is not visible.");
       }
        return followQueryHelper.listUserFollows(currentUserId, targetUserId, FollowQueryHelper.Position.FOLLOWERS,page);
    }

    @CheckUserExistence
    public List<ProfileSummary> listUserFollowing(String targetUserId, int page){
        String currentUserId= authenticatedUserService.getCurrentUser();
        boolean isAllowed= visibilityPolicy.isAllowed(currentUserId,targetUserId);
        if(!isAllowed){
            throw new FollowListNotVisibleException("followings list for user is not visible.");
        }
        return followQueryHelper.listUserFollows(currentUserId, targetUserId, FollowQueryHelper.Position.FOLLOWINGS,page);

    }

}
