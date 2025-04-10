package com.guildnet.backend.features.permission;

import com.guildnet.backend.features.permission.dto.CreatePermissionRequest;
import com.guildnet.backend.features.permission.dto.PermissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public PermissionDTO createPermission(CreatePermissionRequest request) {
        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());

        Permission saved = permissionRepository.save(permission);

        return new PermissionDTO(
                saved.getId(),
                saved.getName(),
                saved.getDescription()
        );
    }

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(permission -> new PermissionDTO(
                        permission.getId(),
                        permission.getName(),
                        permission.getDescription()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));
        return new PermissionDTO(
                permission.getId(),
                permission.getName(),
                permission.getDescription()
        );
    }

    @Override
    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }
}

