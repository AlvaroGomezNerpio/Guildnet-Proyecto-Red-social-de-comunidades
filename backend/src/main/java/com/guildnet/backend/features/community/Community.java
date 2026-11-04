package com.guildnet.backend.features.community;

import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Length;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comunidad temática. Al borrarla, la base de datos elimina en cascada todo su
 * contenido (perfiles, publicaciones, roles, títulos, chat...).
 */
@Entity
@Table(name = "communities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Lob
    @Column(length = Length.LONG32)
    private String description;

    @Lob
    @Column(length = Length.LONG32)
    private String rules;

    @Column(length = 500)
    private String image;

    @Column(length = 500)
    private String banner;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "community_tags", joinColumns = @JoinColumn(name = "community_id"))
    @Column(name = "tag", nullable = false, length = 30)
    private List<String> tags = new ArrayList<>();

    /** Usuario que creó la comunidad; tiene todos los permisos en ella. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Miembros (solo lectura: se usa para contar suscriptores). */
    @Builder.Default
    @OneToMany(mappedBy = "community")
    private List<CommunityProfile> communityProfiles = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
