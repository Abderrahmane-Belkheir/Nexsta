
package com.example.SocialMediaApp.Notification.api.Controllers;

import com.example.SocialMediaApp.Notification.api.dto.NotificationSettings;
import com.example.SocialMediaApp.Notification.application.NotificationSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final NotificationSettingsService notificationSettingsService;

    @PutMapping("/settings")
    public void updateNotificationSettings(@RequestBody NotificationSettings settings) {
        notificationSettingsService.updateNotificationSettings(settings);
    }

    @GetMapping("/settings")
public NotificationSettings getNotificationSettings(){
        return notificationSettingsService.getnotificationsettings();
}

}

