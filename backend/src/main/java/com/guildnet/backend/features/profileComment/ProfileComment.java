package com.guildnet.backend.features.profileComment;

import com.guildnet.backend.features.communityProfile.CommunityProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content; // Contenido del comentario

    // Perfil de comunidad que escribe el comentario (autor)
    @ManyToOne
    @JoinColumn(name = "author_profile_id")
    private CommunityProfile authorProfile;

    // Perfil de comunidad al que se dirige el comentario (destinatario)
    @ManyToOne
    @JoinColumn(name = "target_profile_id")
    private CommunityProfile targetProfile;
}
