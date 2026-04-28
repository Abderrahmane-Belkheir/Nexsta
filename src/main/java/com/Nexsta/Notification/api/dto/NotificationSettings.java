package com.Nexsta.Notification.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettings {
    private Boolean onfollowingrequestAccepted;
    private Boolean onfollowingrequestRejected;
    private Boolean Onfollow;
}
