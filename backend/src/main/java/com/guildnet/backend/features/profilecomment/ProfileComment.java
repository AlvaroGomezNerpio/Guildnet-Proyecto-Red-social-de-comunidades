package com.guildnet.backend.features.profilecomment;

import com.guildnet.backend.features.profile.CommunityProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Perfil de comunidad que escribe el comentario (autor)
    @ManyToOne
    @JoinColumn(name = "author_profile_id")
    private CommunityProfile authorProfile;

    // Perfil de comunidad al que se dirige el comentario (destinatario)
    @ManyToOne
    @JoinColumn(name = "target_profile_id")
    private CommunityProfile targetProfile;
}
