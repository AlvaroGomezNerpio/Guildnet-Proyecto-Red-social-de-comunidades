package com.guildnet.backend.features.rolePermission;

import com.guildnet.backend.features.permission.Permission;
import com.guildnet.backend.features.permission.PermissionRepository;
import com.guildnet.backend.features.role.Role;
import com.guildnet.backend.features.role.RoleRepository;
import com.guildnet.backend.features.rolePermission.dto.RolePermissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public RolePermissionDTO assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);

        RolePermission saved = rolePermissionRepository.save(rolePermission);
        return mapToDTO(saved);
    }


    @Override
    public List<RolePermissionDTO> getPermissionsByRole(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void removeRolePermission(Long id) {
        rolePermissionRepository.deleteById(id);
    }

    private RolePermissionDTO mapToDTO(RolePermission rolePermission) {
        return RolePermissionDTO.builder()
                .id(rolePermission.getId())
                .roleId(rolePermission.getRole().getId())
                .roleName(rolePermission.getRole().getName())
                .permissionId(rolePermission.getPermission().getId())
                .permissionName(rolePermission.getPermission().getName())
                .build();
    }


}

