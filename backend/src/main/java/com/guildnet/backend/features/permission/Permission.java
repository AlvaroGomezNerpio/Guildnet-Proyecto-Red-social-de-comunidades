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

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private PermissionType name;

    private String description;

    @OneToMany(mappedBy = "permission")
    private List<RolePermission> roles;
}
