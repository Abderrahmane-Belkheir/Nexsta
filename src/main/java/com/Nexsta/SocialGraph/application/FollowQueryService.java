package com.Nexsta.SocialGraph.application;

import com.Nexsta.Shared.VisibilityPolicy;
import com.Nexsta.SocialGraph.Exceptions.FollowListNotVisibleException;
import com.Nexsta.SocialGraph.api.dto.FollowQueryResponse;
import com.Nexsta.User.application.AuthenticatedUserService;
import com.Nexsta.Shared.CheckUserExistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowQueryService {

    private final FollowQueryHelper followQueryHelper;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;

    public FollowQueryResponse listCurrentUserFollowers(String cursor) {
        String currentUserId =authenticatedUserService.getCurrentUser();
        return  followQueryHelper.listCurrentUserFollows(currentUserId, FollowQueryHelper.Position.FOLLOWERS,cursor);
    }

    public FollowQueryResponse listCurrentUserFollowings(String cursor) {
       String currentUserId = authenticatedUserService.getCurrentUser();
        return  followQueryHelper.listCurrentUserFollows(currentUserId, FollowQueryHelper.Position.FOLLOWINGS,cursor);
    }

    public FollowQueryResponse listCurrentUserFollowRequests(String cursor) {
        String currentUserId= authenticatedUserService.getCurrentUser();
        return followQueryHelper.
                listCurrentUserPendingFollows(currentUserId,FollowQueryHelper.Position.FOLLOWERS,cursor);
    }

    public FollowQueryResponse listCurrentUserFollowingRequests(String cursor) {
        String currentUserId= authenticatedUserService.getCurrentUser();
        return followQueryHelper.
                listCurrentUserPendingFollows(currentUserId, FollowQueryHelper.Position.FOLLOWINGS,cursor);

    }

    @CheckUserExistence
    public FollowQueryResponse listUserFollowers(String targetUserId, String cursor){
        String currentUserId= authenticatedUserService.getCurrentUser();
       boolean isAllowed= visibilityPolicy.isAllowed(currentUserId, targetUserId);
       if(!isAllowed){
           throw new FollowListNotVisibleException("followers list for user is not visible.");
       }
        return followQueryHelper.listUserFollows(currentUserId, targetUserId, FollowQueryHelper.Position.FOLLOWERS,cursor);
    }

    @CheckUserExistence
    public FollowQueryResponse listUserFollowing(String targetUserId, String cursor){
        String currentUserId= authenticatedUserService.getCurrentUser();
        boolean isAllowed= visibilityPolicy.isAllowed(currentUserId,targetUserId);
        if(!isAllowed){
            throw new FollowListNotVisibleException("followings list for user is not visible.");
        }
        return followQueryHelper.listUserFollows(currentUserId, targetUserId, FollowQueryHelper.Position.FOLLOWINGS,cursor);

    }

}
