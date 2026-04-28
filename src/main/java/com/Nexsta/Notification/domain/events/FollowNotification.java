package com.Nexsta.Notification.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowNotification {
    private String triggerId;
    private String recipientId;
    private notificationType type;

    public enum notificationType{FOLLOW,FOLLOW_REQUESTED,FOLLOWING_ACCEPTED,FOLLOWING_REJECTED}
}
