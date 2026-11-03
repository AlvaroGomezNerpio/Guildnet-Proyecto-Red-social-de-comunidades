package com.guildnet.backend.features.role;

import com.guildnet.backend.features.community.Community;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String textColor;

    private String backgroundColor;

    // Comunidad a la que pertenece este rol (relación muchos a uno)
    @ManyToOne
    @JoinColumn(name = "community_id")
    private Community community;

    // Lista de permisos asociados a este rol (relación uno a muchos)
    @Builder.Default
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RolePermission> permissions = new ArrayList<>();
}
