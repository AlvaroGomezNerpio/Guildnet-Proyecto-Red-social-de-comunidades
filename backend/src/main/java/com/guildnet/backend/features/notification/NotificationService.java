package com.guildnet.backend.features.notification;

import com.guildnet.backend.features.notification.dto.NotificationCreateDTO;
import com.guildnet.backend.features.notification.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    List<NotificationDTO> getNotificationsForProfile(Long profileId);

    int countUnreadNotifications(Long profileId);

    void markAsRead(Long notificationId);

    void deleteNotification(Long notificationId);

    NotificationDTO createNotification(NotificationCreateDTO dto);
}

