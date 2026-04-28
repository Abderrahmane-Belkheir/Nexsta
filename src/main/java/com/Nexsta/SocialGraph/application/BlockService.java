package com.Nexsta.SocialGraph.application;

import com.Nexsta.SocialGraph.Exceptions.BadFollowRequestException;
import com.Nexsta.User.application.AuthenticatedUserService;
import com.Nexsta.Shared.CheckUserExistence;
import com.Nexsta.SocialGraph.domain.Block;
import com.Nexsta.SocialGraph.persistence.BlocksRepo;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {
    private final BlocksRepo blocksRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final FollowRepo followRepo;
    private final FollowingService followService;

    @CheckUserExistence
    public void block(String targetUserId) {
        String currentUserId = authenticatedUserService.getCurrentUser();
        if(currentUserId.equals(targetUserId)){throw new BadFollowRequestException("you cant block yourself");}
        boolean alreadyBlocked= blocksRepo.
                existsByBlockerIdAndBlockedId(currentUserId,targetUserId);
        if(alreadyBlocked){
           return;
        }

        Block block = new Block(currentUserId,targetUserId);

        boolean follower=followRepo.existsByFollowerIdAndFollowingId(currentUserId,targetUserId);

        if(follower){
            followService.toggleFollow(targetUserId);
        }

        boolean followed=followRepo.existsByFollowerIdAndFollowingId(targetUserId,currentUserId);

        if (followed){
            followService.removeFollower(targetUserId);
        }
        blocksRepo.save(block);
    }


    @CheckUserExistence
    public void unBlock(String targetUserId) {
        String currentUserId = authenticatedUserService.getCurrentUser();
        blocksRepo.
                deleteByBlockerIdAndBlockedId(currentUserId, targetUserId);
    }
}
