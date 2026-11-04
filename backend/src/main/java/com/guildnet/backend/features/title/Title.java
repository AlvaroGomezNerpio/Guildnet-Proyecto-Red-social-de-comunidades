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

    @Builder.Default
    @ManyToMany(mappedBy = "titles")
    private List<CommunityProfile> profiles = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;
}
