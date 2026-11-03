package com.guildnet.backend.features.rolePermission.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRolePermissionRequest(@NotNull Long roleId, @NotNull Long permissionId) {
}
