package com.guildnet.backend.features.role;

import com.guildnet.backend.features.permission.Permission;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Rol al que se le asigna un permiso (relación muchos a uno)
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    // Permiso asignado al rol (relación muchos a uno)
    @ManyToOne
    @JoinColumn(name = "permission_id")
    private Permission permission;
}
