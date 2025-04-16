package com.guildnet.backend.features.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Obtener todas las notificaciones recibidas por un perfil
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // Contar cuántas notificaciones no leídas tiene un perfil
    int countByReceiverIdAndItIsReadFalse(Long receiverId); // <--- CAMBIO AQUÍ
}
