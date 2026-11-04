package com.guildnet.backend.features.title;

import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.profile.CommunityProfile;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** Título decorativo que se puede asignar a los miembros de una comunidad. */
@Entity
@Table(name = "titles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Title {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 7)
    private String textColor;

    @Column(nullable = false, length = 7)
    private String backgroundColor;

    /** Regla de asignación automática (null: título manual). */
    @Enumerated(EnumType.STRING)
    @Column(name = "auto_metric", length = 20)
    private TitleMetric autoMetric;

    @Column(name = "auto_threshold")
    private Integer autoThreshold;

    @Builder.Default
    @ManyToMany(mappedBy = "titles")
    private List<CommunityProfile> profiles = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;
}
