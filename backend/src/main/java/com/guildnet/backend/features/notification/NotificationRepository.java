package com.guildnet.backend.features.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Notificaciones recibidas por un perfil, de la más reciente a la más antigua. */
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    int countByReceiverIdAndReadFalse(Long receiverId);
}
