package com.guildnet.backend.features.role.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRolePermissionRequest(@NotNull Long roleId, @NotNull Long permissionId) {
}
