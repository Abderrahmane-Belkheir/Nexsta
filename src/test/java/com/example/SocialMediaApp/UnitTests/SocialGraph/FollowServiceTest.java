package com.example.SocialMediaApp.UnitTests.SocialGraph;

import com.example.SocialMediaApp.Notification.domain.events.FollowNotification;
import com.example.SocialMediaApp.Profile.api.dto.ProfileDetails;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.Shared.Exceptions.BadFollowRequestException;
import com.example.SocialMediaApp.Shared.Exceptions.NoRelationShipException;
import com.example.SocialMediaApp.SocialGraph.Exceptions.BadFollowRequestException;
import com.example.SocialMediaApp.SocialGraph.Exceptions.NoRelationShipException;
import com.example.SocialMediaApp.SocialGraph.application.FollowQueryHelper;
import com.example.SocialMediaApp.SocialGraph.application.FollowerService;
import com.example.SocialMediaApp.SocialGraph.application.FollowingService;
import com.example.SocialMediaApp.SocialGraph.application.cache.FollowCacheUpdater;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.domain.RelationshipStatus;
import com.example.SocialMediaApp.SocialGraph.domain.events.FollowAdded;
import com.example.SocialMediaApp.SocialGraph.domain.events.FollowRemoved;
import com.example.SocialMediaApp.SocialGraph.persistence.BlocksRepo;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FollowServiceTest {

    @Mock
    private FollowRepo followRepo;
    @Mock
    private ProfileRepo profileRepo;
    @Mock
    private BlocksRepo blocksRepo;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private FollowCacheUpdater followCacheUpdater;
    @InjectMocks
    private FollowerService followRequestService;
    @InjectMocks
    private FollowingService followService;

    private final String currentUserId=UUID.randomUUID().toString();
    private final String targetUserId = UUID.randomUUID().toString();

    @BeforeEach
    public  void setCurrentUser() {
       when(authenticatedUserService.getCurrentUser()).thenReturn(currentUserId);
    }

    // FOLLOW CREATION TESTS
    @Nested
    @DisplayName("Follow Creation Tests")
    class FollowCreationTests {

        @Test
        public void Follow_yourself_throwsBadFollowException(){
            Exception exception=assertThrows(com.example.SocialMediaApp.SocialGraph.Exceptions.BadFollowRequestException.class,
                    ()->followService.Follow(currentUserId));
            assertEquals("you cant follow yourself", exception.getMessage());
        }

        @Test
        public void follow_alreadyFollowedUser_throwsBadFollowException() {
            when(followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId, targetUserId, Follow.Status.ACCEPTED))
                    .thenReturn(true);
            assertthrows(com.example.SocialMediaApp.SocialGraph.Exceptions.BadFollowRequestException.class,
                    () -> followService.Follow(targetUserId), "Already followed");
        }

        @Test
        public void follow_pendingFollowRequest_throwsBadFollowRequestException() {
          lenient().when(followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId, targetUserId, Follow.Status.PENDING))
                    .thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(targetUserId), "request already sent");
        }

        @Test
        public void follow_userBlockedByCurrentUser_throwsBadFollowRequestException() {
            lenient().when(blocksRepo.existsByBlockerIdAndBlockedId(currentUserId,targetUserId)).thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(targetUserId), "You cant follow this User");
        }

        @Test
        public void follow_userHasBlockedCurrentUser_throwsBadFollowRequestException() {
            when(blocksRepo.existsByBlockerIdAndBlockedId(targetUserId,currentUserId)).thenReturn(true);
            assertthrows(BadFollowRequestException.class,
                    () -> followService.Follow(targetUserId), "You cant follow this User");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true,false})
        public void follow_public_privateProfiles(boolean Public){
            when(profileRepo.existsByUserIdAndIsprivateFalse(targetUserId)).thenReturn(Public);
            ProfileDetails profileDetails=followService.Follow(targetUserId);
            assertEquals(targetUserId,profileDetails.getUserId());
            ArgumentCaptor<Follow> captor1=ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor1.capture());
            Follow follow=captor1.getValue();
            assertEquals(follow.getFollower().getId(),currentUserId);
            assertEquals(follow.getFollowing().getId(),targetUserId);
            ArgumentCaptor<FollowNotification> captor2=ArgumentCaptor.forClass(FollowNotification.class);
            verify(eventPublisher).publishEvent(captor2.capture());
            FollowNotification followNotification=captor2.getValue();
            assertEquals(followNotification.getTriggerId(),currentUserId);
            assertEquals(followNotification.getRecipientId(),targetUserId);
            if(Public){
                assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
               assertEquals(FollowNotification.notificationType.FOLLOW,followNotification.getType());
               assertEquals(RelationshipStatus.FOLLOWING, profileDetails.getRelationship());
               verify(eventPublisher).publishEvent(any(FollowAdded.class));
               ArgumentCaptor<FollowQueryHelper.Position> captor3=ArgumentCaptor.forClass(FollowQueryHelper.Position.class);
                ArgumentCaptor<FollowCacheUpdater.UpdateType> captor4=ArgumentCaptor.forClass(FollowCacheUpdater.UpdateType.class);
                ArgumentCaptor<String> captor5=ArgumentCaptor.forClass(String.class);
                verify(followCacheUpdater).UpdateCount(captor3.capture(),captor5.capture(),captor4.capture());
                //assertEquals();

            }else{
                assertEquals(Follow.Status.PENDING,follow.getStatus());
                assertEquals(FollowNotification.notificationType.FOLLOW_REQUESTED,followNotification.getType());
                assertEquals(RelationshipStatus.FOLLOW_REQUESTED,profileDetails.getRelationship());
            }
        }

    }

    // FOLLOW REMOVAL TESTS
    @Nested
    @DisplayName("Follow Removal Tests")
    class FollowRemovalTests {
        @Test
        public void unfollow_notfollowing_throwsNoRelationShipException(){
            when(followRepo.findByFollowerIdAndFollowingId(currentUserId,targetUserId)).thenReturn(Optional.empty());
            assertThrows(com.example.SocialMediaApp.SocialGraph.Exceptions.NoRelationShipException.class,()->followService.unFollow(targetUserId),"No relation with user found");
        }

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void unfollow_pending_accepted_followings(Follow.Status status){
            Follow follow =new Follow(currentUserId,targetUserId);
            follow.setStatus(status);
            when(followRepo.findByFollowerIdAndFollowingId(currentUserId,targetUserId)).thenReturn(Optional.of(follow));
            followService.unFollow(targetUserId);
            verify(followRepo).delete(follow);
            if(status== Follow.Status.ACCEPTED){
                verify(eventPublisher).publishEvent(any(FollowRemoved.class));
            }
        }


        @Test
        public void removefollower_notfollower_throwsNoRelationShipException(){
            when(followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId)).thenReturn(Optional.empty());
            assertThrows(NoRelationShipException.class,()->followService.removeFollower(targetUserId),"No relation with user found");
        }

        @ParameterizedTest
        @EnumSource(Follow.Status.class)
        public void removefollower_pending_accepted_followers(Follow.Status status){
            Follow follow=new Follow(targetUserId,currentUserId);
            follow.setStatus(status);
            when(followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId)).thenReturn(Optional.of(follow));
            followService.removeFollower(targetUserId);
            verify(followRepo).delete(follow);
            if(status== Follow.Status.ACCEPTED){
                ArgumentCaptor<FollowRemoved> captor=ArgumentCaptor.forClass(FollowRemoved.class);
                verify(eventPublisher).publishEvent(captor.capture());
                FollowRemoved followRemoved=captor.getValue();
                assertThat(follow).usingRecursiveComparison().isEqualTo(followRemoved.getFollow());

            }else{
                ArgumentCaptor<FollowNotification> captor=ArgumentCaptor.forClass(FollowNotification.class);
                verify(eventPublisher).publishEvent(captor.capture());
                FollowNotification followNotification=captor.getValue();
                assertEquals(FollowNotification.notificationType.FOLLOWING_REJECTED,followNotification.getType());
                assertEquals(targetUserId,followNotification.getRecipientId());
                assertEquals(currentUserId,followNotification.getTriggerId());
            }
        }

    }

    @Nested
    @Description("Follow request Tests")
    class FollowRequestTests{

       // ACCEPT FOLLOW METHOD TESTS
       @Test
       public void accept_not_requested_throwsBadFollowRequestException(){
           when(followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId)).thenReturn(Optional.empty());
           assertthrows(NoRelationShipException.class,()->followRequestService.acceptFollow(targetUserId),"No relation with user found");
       }

        @Test
        public void acceptAlreadyFollower_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId)).
                    thenReturn(Optional.of(new Follow(targetUserId,currentUserId, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.acceptFollow(targetUserId),"couldn't perform accept follow action on this user");
        }

        @Test
        public void accept_Follow_save_update_cache(){

            when(followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId)).
                    thenReturn(Optional.of(new Follow(targetUserId,currentUserId, Follow.Status.PENDING)));

            assertDoesNotThrow(()->followRequestService.acceptFollow(targetUserId));
            ArgumentCaptor<Follow> captor=ArgumentCaptor.forClass(Follow.class);
            verify(followRepo).save(captor.capture());
            Follow follow=captor.getValue();
            assertEquals(Follow.Status.ACCEPTED, follow.getStatus());
            assertEquals(follow.getFollower().getId(),targetUserId);
            assertEquals(follow.getFollowing().getId(),currentUserId);

            ArgumentCaptor<Object> captor1=ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher,times(2)).publishEvent(captor1.capture());
          captor1.getAllValues().forEach(o->{
              if(o instanceof FollowNotification followNotification){
                  assertEquals(FollowNotification.notificationType.FOLLOWING_ACCEPTED,followNotification.getType());
                  assertEquals(targetUserId,followNotification.getRecipientId());
                  assertEquals(currentUserId,followNotification.getTriggerId());
              } else if (o instanceof FollowAdded followAdded){
                  assertThat(follow).usingRecursiveComparison().isEqualTo(followAdded.getFollow());
              }
          });

        }


        // REJECT FOLLOW METHOD TESTS

        @Test
        public void reject_not_requested_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId)).thenReturn(Optional.empty());
            assertthrows(NoRelationShipException.class,()->followRequestService.rejectFollow(targetUserId),"No relation with user found");
        }

        @Test
        public void rejectAlreadyFollower_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId)).thenReturn(Optional.of(new Follow(targetUserId,currentUserId, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.rejectFollow(targetUserId),"couldn't perform reject follow action on this user");
        }

        @Test
        public void reject_Follow_save_update_cache(){

            when(followRepo.findByFollowerIdAndFollowingId(targetUserId,currentUserId)).
                    thenReturn(Optional.of(new Follow(targetUserId,currentUserId, Follow.Status.PENDING)));
            assertDoesNotThrow(()->followRequestService.rejectFollow(targetUserId));
            verify(followRepo).delete(any(Follow.class));
            ArgumentCaptor<FollowNotification> captor=ArgumentCaptor.forClass(FollowNotification.class);
            verify(eventPublisher).publishEvent(captor.capture());
            FollowNotification notification=captor.getValue();
            assertEquals(FollowNotification.notificationType.FOLLOWING_REJECTED, notification.getType());
            assertEquals(notification.getTriggerId(),currentUserId);
            assertEquals(notification.getRecipientId(),targetUserId);
        }

        // UNSEND FOLLOWING REQUESTS

        @Test
        public void unsend_not_requested_throwsBadFollowRequestException(){
            when(followRepo.findByFollowerIdAndFollowingId(currentUserId,targetUserId)).thenReturn(Optional.empty());
            assertthrows(NoRelationShipException.class,()->followRequestService.unsendFollowingRequest(targetUserId),"No relation with user found");
        }

        @Test
        public void unsendAlreadyFollowing(){
            when(followRepo.findByFollowerIdAndFollowingId(currentUserId,targetUserId)).thenReturn(Optional.of(new Follow(currentUserId,targetUserId, Follow.Status.ACCEPTED)));
            assertthrows(BadFollowRequestException.class,()->followRequestService.unsendFollowingRequest(targetUserId),"couldn't perform unsend follow request on this user");
        }

        @Test
        public void UnsendFollowSuccess_removeFollow(){
           Follow follow=new Follow(currentUserId,targetUserId, Follow.Status.PENDING);
            when(followRepo.findByFollowerIdAndFollowingId(currentUserId,targetUserId)).
                    thenReturn(Optional.of(follow));
            assertDoesNotThrow(()->followRequestService.unsendFollowingRequest(targetUserId));
            verify(followRepo).delete(follow);
        }

    }


    // Helper method for asserting exceptions
    public static void assertthrows(Class<? extends Exception> expected, Executable executable, String expectedMessage) {
        Exception exception = assertThrows(expected, executable);
        assertEquals(expectedMessage, exception.getMessage());
    }

}
