package com.guildnet.backend.features.rolePermission;


import com.guildnet.backend.features.rolePermission.dto.RolePermissionDTO;

import java.util.List;

public interface RolePermissionService {
    RolePermissionDTO assignPermissionToRole(Long roleId, Long permissionId);
    List<RolePermissionDTO> getPermissionsByRole(Long roleId);
    void removeRolePermission(Long id);
}


