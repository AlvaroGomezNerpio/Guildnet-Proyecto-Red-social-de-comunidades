package com.guildnet.backend.features.profile;

import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Length;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Perfil de un usuario dentro de una comunidad (nombre, imagen y descripción propios).
 * Al borrarlo, la base de datos elimina en cascada sus publicaciones, comentarios, likes...
 */
@Entity
@Table(name = "community_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Lob
    @Column(length = Length.LONG32)
    private String description;

    @Column(length = 500)
    private String profileImage;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    /** Rol del miembro en la comunidad (opcional). */
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    /** Título que el miembro muestra destacado (debe ser uno de los suyos). */
    @ManyToOne
    @JoinColumn(name = "featured_title_id")
    private Title featuredTitle;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "profile_titles",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "title_id")
    )
    private List<Title> titles = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
