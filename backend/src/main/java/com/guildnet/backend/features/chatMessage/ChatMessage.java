package com.guildnet.backend.features.chatMessage;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.communityProfile.CommunityProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private LocalDateTime sentAt; // Fecha y hora en que se envió el mensaje

    // Comunidad en la que se ha enviado el mensaje
    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;

    // Perfil de comunidad del usuario que envió el mensaje
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private CommunityProfile profile;
}
