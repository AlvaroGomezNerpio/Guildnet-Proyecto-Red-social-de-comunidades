package com.guildnet.backend.features.permission;

import com.guildnet.backend.features.rolePermission.RolePermission;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    private String name; // Nombre de la permisión (por ejemplo: "EDIT_POST", "DELETE_USER", etc.)

    private String description;

    // Lista de asociaciones con roles.
    // Esta relación es parte de una tabla intermedia (RolePermission) para modelar la relación muchos a muchos entre Role y Permission
    @OneToMany(mappedBy = "permission")
    private List<RolePermission> roles;
}
