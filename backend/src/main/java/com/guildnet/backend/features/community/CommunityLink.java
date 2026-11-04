package com.guildnet.backend.features.community;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Enlace externo de una comunidad (Discord, web, redes...). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityLink {

    @Column(nullable = false, length = 40)
    private String label;

    @Column(nullable = false, length = 500)
    private String url;
}
