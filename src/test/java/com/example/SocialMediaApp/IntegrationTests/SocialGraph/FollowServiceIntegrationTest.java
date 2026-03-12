package com.example.SocialMediaApp.IntegrationTests.SocialGraph;

import com.example.SocialMediaApp.IntegrationTests.TestContainerConfig;
import com.example.SocialMediaApp.SocialGraph.Exceptions.BadFollowRequestException;
import com.example.SocialMediaApp.SocialGraph.application.FollowQueryHelper;
import com.example.SocialMediaApp.SocialGraph.application.FollowRequestService;
import com.example.SocialMediaApp.SocialGraph.application.FollowService;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import static com.example.SocialMediaApp.UnitTests.SocialGraph.FollowServiceTest.assertthrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class FollowServiceIntegrationTest extends TestContainerConfig {
    private final FollowRepo followRepo;
    private final UserRepo userRepo;
    private final FollowService followService;
    private final FollowTestHelper followTestHelper;
    private final FollowRequestService followRequestService;
    private final AuthenticatedUserService authenticatedUserService;
    public  enum ProfileType { PRIVATE, PUBLIC }

    @BeforeEach
    public  void setAuthentication(){
        User currentUser = userRepo.saveAndFlush(new User(UUID.randomUUID().toString()));
        Jwt jwt = Jwt.withTokenValue("test_token").subject(currentUser.getUsername())
                .header("alg","none")
                .build();
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Nested
    class FollowCreationTests {


        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void follow_recordFound_throwsBadFollowException(Follow.Status followStatus){
            String targetUserId =followTestHelper.createFollowRecord(followStatus, FollowQueryHelper.Position.FOLLOWINGS);
            String expectedMessage=followStatus== Follow.Status.ACCEPTED?"Already followed":"request already sent";
    assertthrows(BadFollowRequestException.class,
            () -> followService.Follow(targetUserId), expectedMessage);
}

        /*
             "true"  current user is blocked by the user he wants to follow
             "false" current user blocked the // // //   //    /   //
        */
        @ParameterizedTest
        @ValueSource(booleans = {true,false})
        public void follow_blockFound_throwsBadFollowRequestException(boolean iscurrentBlocked){
            String expectedMessage="You cant follow this User";
            String targetUserId = followTestHelper.createBlockRecord(iscurrentBlocked);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(targetUserId), expectedMessage);
        }

        @ParameterizedTest
        @EnumSource(ProfileType.class)
        public void follow_profileTypes(ProfileType profileType){
            String currentUserId=authenticatedUserService.getCurrentUser();
            String targetUserId =followTestHelper.createUserProfile(profileType);
            followService.Follow(targetUserId);
            if(profileType== ProfileType.PUBLIC){
                assertTrue(followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId, targetUserId, Follow.Status.ACCEPTED));
                return ;
            }
            assertTrue(followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId, targetUserId, Follow.Status.PENDING));
        }
    }

    @Nested
    class FollowRemovalTests {
        @ParameterizedTest
      @EnumSource(Follow.Status.class)
        public void unfollow(Follow.Status followStatus){
            String currentUserId = authenticatedUserService.getCurrentUser();
            String targetUserId =followTestHelper.createFollowRecord(followStatus, FollowQueryHelper.Position.FOLLOWINGS);
            followService.UnFollow(targetUserId);
            assertFalse(followRepo.existsByFollowerIdAndFollowingId(currentUserId,targetUserId));
       // assertFalse(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":following:" + user.getKeycloakId()));
         //   assertFalse(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":follower:" + currentUser.getKeycloakId()));
}

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void removeFollower(Follow.Status followStatus){
            String currentUserId = authenticatedUserService.getCurrentUser();
            String targetUserId=followTestHelper.createFollowRecord(followStatus, FollowQueryHelper.Position.FOLLOWERS);
            followService.removefollower(targetUserId);
            assertFalse(followRepo.existsByFollowerIdAndFollowingId(targetUserId,currentUserId));
           // assertFalse(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":following:" + currentUser.getKeycloakId()));
           // assertFalse(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":follower:" + user.getKeycloakId()));
        }
    }
    @Nested
    class FollowRequestTests{

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void acceptFollower(Follow.Status followStatus){
            String currentUserId = authenticatedUserService.getCurrentUser();
            String targetUserId= followTestHelper.createFollowRecord(followStatus, FollowQueryHelper.Position.FOLLOWERS);
            if(followStatus== Follow.Status.ACCEPTED){
                assertthrows(BadFollowRequestException.class, () ->followRequestService.acceptFollow(targetUserId),
                        "couldn't perform accept follow action on this user");
                return;
            }
            followRequestService.acceptFollow(targetUserId);
            assertTrue(followRepo.existsByFollowerIdAndFollowingIdAndStatus(targetUserId,currentUserId, Follow.Status.ACCEPTED));
          //  assertTrue(redisTemplate.hasKey("user:" + user.getKeycloakId() + ":following:" + currentUser.getKeycloakId()));
          //  assertTrue(redisTemplate.hasKey("user:" + currentUser.getKeycloakId() + ":follower:" + user.getKeycloakId()));
        }

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void rejectFollower(Follow.Status followStatus){
           String currentUserId = authenticatedUserService.getCurrentUser();
            String targetUserId= followTestHelper.createFollowRecord(followStatus, FollowQueryHelper.Position.FOLLOWERS);
            if(followStatus== Follow.Status.ACCEPTED){
                assertthrows(BadFollowRequestException.class, () ->followRequestService.rejectFollow(targetUserId),
                        "couldn't perform reject follow action on this user");
                return;
            }
            followRequestService.rejectFollow(targetUserId);
            assertFalse(followRepo.existsByFollowerIdAndFollowingId(targetUserId,currentUserId));
        }

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void unsendFollow(Follow.Status followStatus){
            String currentUserId=authenticatedUserService.getCurrentUser();
            String targetUserId= followTestHelper.createFollowRecord(followStatus, FollowQueryHelper.Position.FOLLOWINGS);

            if(followStatus== Follow.Status.ACCEPTED){
                assertthrows(BadFollowRequestException.class, () ->followRequestService.unsendFollowingRequest(targetUserId),
                        "couldn't perform unsend follow request on this user");
                return;
            }
            followRequestService.unsendFollowingRequest(targetUserId);
            assertFalse(followRepo.existsByFollowerIdAndFollowingId(currentUserId,targetUserId));
        }
    }
    /*
    Tests the user existence validation aspect "@CheckUserExistence" via a follow method unit test for the aspect will be added later.
     */
    @Test
    public void test_user_existence(){
        assertthrows(com.example.SocialMediaApp.User.Exceptions.UserNotFoundException.class,()-> followService.Follow("test"),"User not found");
    }
}
