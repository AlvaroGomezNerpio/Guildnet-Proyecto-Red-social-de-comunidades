package com.guildnet.backend.features.permission;

import com.guildnet.backend.features.permission.dto.CreatePermissionRequest;
import com.guildnet.backend.features.permission.dto.PermissionDTO;

import java.util.List;

public interface PermissionService {
    PermissionDTO createPermission(CreatePermissionRequest request);
    List<PermissionDTO> getAllPermissions();
    PermissionDTO getPermissionById(Long id);
    void deletePermission(Long id);
}

