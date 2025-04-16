package com.guildnet.backend.features.notification.dto;

import com.guildnet.backend.features.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCreateDTO {
    private String message;
    private NotificationType type;
    private Long receiverProfileId;
    private Long senderProfileId; // puede ser null si es una notificación del sistema
}
