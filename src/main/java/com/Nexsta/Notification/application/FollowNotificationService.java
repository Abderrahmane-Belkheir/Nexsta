package com.Nexsta.Notification.application;
import com.Nexsta.Notification.api.dto.NotificationResponse;
import com.Nexsta.Notification.domain.events.FollowNotification;
import com.Nexsta.Profile.domain.cache.ProfileInfo;
import com.Nexsta.Shared.MediaUrlResolver;
import com.Nexsta.User.application.UserActivityTracker;
import com.Nexsta.Notification.domain.NotificationsSettings;
import com.Nexsta.Notification.persistence.NotificationSettingsRepo;
import com.Nexsta.Profile.application.ProfileQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowNotificationService {

    private final SimpMessagingTemplate MessagingTemplate;
    private final NotificationSettingsRepo notificationSettingsRepo;
    private final ProfileQueryService profileQueryService;
    private final UserActivityTracker userActivityService;
    private final MediaUrlResolver mediaUrlResolver;

    @Async
    @EventListener
    public void FollowNotificationProcessing(FollowNotification notificationEvent) {

        String recipientId= notificationEvent.getRecipientId();
        boolean Send= canSendNotification(recipientId,notificationEvent.getType());
        if(!Send){
            return;
        }

        String triggerId=notificationEvent.getTriggerId();
        ProfileInfo profileInfo=profileQueryService.getUserProfileInfo(triggerId);
        StringBuilder message=new StringBuilder(profileInfo.getUsername());
        switch(notificationEvent.getType()){
            case FOLLOW->message.append(" Started Following you");
            case FOLLOW_REQUESTED -> message.append(" Requested Following you");
            case FOLLOWING_ACCEPTED -> message.append(" Accepted Your follow");
            case FOLLOWING_REJECTED -> message.append(" Rejected Your follow");
        }
        log.info("publishing "+message +" to "+recipientId);
        NotificationResponse notification=new NotificationResponse(message.toString(),mediaUrlResolver.resolveFullUrl(profileInfo.getAvatarPath()),profileInfo.getUserId());
        MessagingTemplate.convertAndSendToUser(recipientId,"/queue/notifications",notification);
    }

    private boolean canSendNotification(String userId, FollowNotification.notificationType notificationType){
       boolean Online= userActivityService.getUserStatus(userId);
       if(!Online){
           log.error("user "+userId+" is not online ");
           return false;
       }

       NotificationsSettings notificationsSettings=notificationSettingsRepo.findByUserId(userId);
       switch (notificationType){
           case FOLLOW,FOLLOW_REQUESTED-> {
               if(!notificationsSettings.getOnfollow()){
                   return false;
               }
           }
           case FOLLOWING_ACCEPTED->{
                   if(!notificationsSettings.getOnfollowingrequestAccepted()){
                       return false;
                   }
               }
           case FOLLOWING_REJECTED -> {
               if(!notificationsSettings.getOnfollowingrequestRejected()){
                   return false;
               }
           }
       }
       return true;
    }

}
