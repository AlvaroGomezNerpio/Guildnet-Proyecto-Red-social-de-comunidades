package com.guildnet.backend.features.category;

import com.guildnet.backend.features.community.Community;
import jakarta.persistence.*;
import lombok.*;

/** Categoría oficial de publicaciones de una comunidad ("Guía", "Pregunta"...). */
@Entity
@Table(name = "post_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "text_color", nullable = false, length = 7)
    private String textColor;

    @Column(name = "background_color", nullable = false, length = 7)
    private String backgroundColor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;
}
