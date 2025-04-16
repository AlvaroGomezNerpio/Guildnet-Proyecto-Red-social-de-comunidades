package com.guildnet.backend.features.notification;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
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

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean itIsRead;

    private LocalDateTime createdAt;

    // Perfil que recibe la notificación
    @ManyToOne
    @JoinColumn(name = "receiver_profile_id", nullable = false)
    private CommunityProfile receiver;

    // Perfil que genera la notificación (opcional)
    @ManyToOne
    @JoinColumn(name = "sender_profile_id")
    private CommunityProfile sender;
}