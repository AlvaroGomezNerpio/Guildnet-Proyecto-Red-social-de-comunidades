package com.guildnet.backend.features.role.dto;

public record RolePermissionDTO(
        Long id,
        Long roleId,
        String roleName,
        Long permissionId,
        String permissionName
) {
}
