package com.example.SocialMediaApp.Notification.persistence;

import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingsRepo extends JpaRepository<NotificationsSettings,UUID> {
   NotificationsSettings findByUserId(String userId);
}
