package com.guildnet.backend.features.notification;

import com.guildnet.backend.features.profile.CommunityProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    /** Perfil que recibe la notificación. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_profile_id", nullable = false)
    private CommunityProfile receiver;

    /** Perfil que la provoca; null en las notificaciones del sistema o si ese perfil ya no existe. */
    @ManyToOne
    @JoinColumn(name = "sender_profile_id")
    private CommunityProfile sender;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
