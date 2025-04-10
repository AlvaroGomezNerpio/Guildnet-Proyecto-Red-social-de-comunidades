package com.guildnet.backend.features.notification;

import com.guildnet.backend.features.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    // Indicador de si el usuario ha leído la notificación (true = leída)
    private boolean isRead;

    // Fecha y hora en que se creó la notificación
    private LocalDateTime createdAt;

    // Usuario al que va dirigida la notificación
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}