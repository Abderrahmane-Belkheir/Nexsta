package com.Nexsta.Notification.persistence;

import com.Nexsta.Notification.domain.NotificationsSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationSettingsRepo extends JpaRepository<NotificationsSettings,UUID> {
   NotificationsSettings findByUserId(String userId);
}
