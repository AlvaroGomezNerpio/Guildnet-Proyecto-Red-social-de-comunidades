package com.guildnet.backend.features.permission;

import jakarta.persistence.*;
import lombok.*;

/** Permiso del catálogo. Los registros se crean con la migración V2__permisos.sql. */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private PermissionType name;

    @Column(nullable = false)
    private String description;
}
