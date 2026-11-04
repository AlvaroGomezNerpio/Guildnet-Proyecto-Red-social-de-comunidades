package com.guildnet.backend.features.profilecomment;

import com.guildnet.backend.features.profile.CommunityProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** Comentario que un miembro deja en el perfil de otro dentro de la misma comunidad. */
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

    @Column(nullable = false, length = 2000)
    private String content;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_profile_id", nullable = false)
    private CommunityProfile authorProfile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_profile_id", nullable = false)
    private CommunityProfile targetProfile;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
