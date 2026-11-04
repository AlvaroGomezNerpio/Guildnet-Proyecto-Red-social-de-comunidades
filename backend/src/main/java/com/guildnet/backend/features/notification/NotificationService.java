package com.guildnet.backend.features.notification;

import com.guildnet.backend.common.exception.ForbiddenException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.common.mapper.DtoMapper;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.notification.dto.NotificationDTO;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Notificaciones de los perfiles. Las genera el propio backend cuando ocurre algo
 * (comentario, like, asignación de rol o título...), nunca el cliente.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccessGuard accessGuard;

    /**
     * Crea una notificación para {@code receiver}. No se notifica a uno mismo.
     *
     * @param sender perfil que provoca la notificación; {@code null} si es del sistema.
     */
    public void notify(CommunityProfile receiver, CommunityProfile sender, NotificationType type, String message) {
        if (receiver == null || (sender != null && Objects.equals(receiver.getId(), sender.getId()))) {
            return;
        }
        notificationRepository.save(Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(type)
                .message(message)
                .read(false)
                .build());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getForProfile(Long profileId, User user) {
        accessGuard.requireOwnProfile(user, profileId);
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(profileId).stream()
                .map(NotificationService::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public int countUnread(Long profileId, User user) {
        accessGuard.requireOwnProfile(user, profileId);
        return notificationRepository.countByReceiverIdAndReadFalse(profileId);
    }

    public void markAsRead(Long notificationId, User user) {
        loadOwn(notificationId, user).setRead(true);
    }

    public void delete(Long notificationId, User user) {
        notificationRepository.delete(loadOwn(notificationId, user));
    }

    private Notification loadOwn(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada"));
        if (!accessGuard.isOwnProfile(user, notification.getReceiver())) {
            throw new ForbiddenException("Esa notificación no es tuya");
        }
        return notification;
    }

    private static NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .receiver(DtoMapper.toProfileDTO(notification.getReceiver()))
                .sender(DtoMapper.toProfileDTO(notification.getSender()))
                .build();
    }
}
