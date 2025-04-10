package com.guildnet.backend.features.rolePermission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRolePermissionRequest {
    private Long roleId;
    private Long permissionId;
}

