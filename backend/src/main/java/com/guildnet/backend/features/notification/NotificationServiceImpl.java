package com.guildnet.backend.features.notification;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.communityProfile.CommunityProfileRepository;
import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.notification.dto.NotificationCreateDTO;
import com.guildnet.backend.features.notification.dto.NotificationDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CommunityProfileRepository profileRepository;

    @Override
    public List<NotificationDTO> getNotificationsForProfile(Long profileId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(profileId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int countUnreadNotifications(Long profileId) {
        return notificationRepository.countByReceiverIdAndItIsReadFalse(profileId); // <--- CAMBIO AQUÍ
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setItIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public NotificationDTO createNotification(NotificationCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El DTO de notificación no puede ser nulo");
        }

        if (dto.getReceiverProfileId() == null) {
            throw new IllegalArgumentException("El ID del perfil receptor es requerido");
        }

        CommunityProfile receiver = profileRepository.findById(dto.getReceiverProfileId())
                .orElseThrow(() -> new NoSuchElementException("Perfil receptor no encontrado: " + dto.getReceiverProfileId()));

        CommunityProfile sender = null;
        if (dto.getSenderProfileId() != null) {
            sender = profileRepository.findById(dto.getSenderProfileId())
                    .orElseThrow(() -> new NoSuchElementException("Perfil emisor no encontrado: " + dto.getSenderProfileId()));
        }

        Notification notification = Notification.builder()
                .message(dto.getMessage())
                .type(dto.getType())
                .itIsRead(false)
                .createdAt(LocalDateTime.now())
                .receiver(receiver)
                .sender(sender)
                .build();

        Notification saved = notificationRepository.save(notification);
        return mapToDTO(saved);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.isItIsRead())
                .createdAt(notification.getCreatedAt())
                .receiver(mapToCommunityProfileDTO(notification.getReceiver()))
                .sender(notification.getSender() != null ? mapToCommunityProfileDTO(notification.getSender()) : null)
                .build();
    }

    private CommunityProfileDTO mapToCommunityProfileDTO(CommunityProfile profile) {
        return new CommunityProfileDTO(
                profile.getId(),
                profile.getUsername(),
                profile.getDescription(),
                profile.getProfileImage(),
                profile.getFeaturedTitle() != null ? new com.guildnet.backend.features.title.dto.TitleDTO(
                        profile.getFeaturedTitle().getId(),
                        profile.getFeaturedTitle().getTitle(),
                        profile.getFeaturedTitle().getTextColor(),
                        profile.getFeaturedTitle().getBackgroundColor(),
                        profile.getFeaturedTitle().getCommunity().getId()) : null,
                profile.getUser().getId(),
                profile.getCommunity().getId(),
                profile.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()),
                profile.getTitles().stream().map(t -> t.getTitle()).collect(Collectors.toList())
        );
    }
}

