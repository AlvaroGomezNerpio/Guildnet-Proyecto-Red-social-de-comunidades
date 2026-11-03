package com.guildnet.backend.features.permission;

import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.features.permission.dto.PermissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catálogo de permisos (solo lectura). Los permisos se definen en {@link PermissionType}
 * y se cargan al arrancar con {@link PermissionInitializer}.
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionRepository permissionRepository;

    @GetMapping
    public List<PermissionDTO> getAll() {
        return permissionRepository.findAll().stream().map(PermissionController::toDTO).toList();
    }

    @GetMapping("/{id}")
    public PermissionDTO getById(@PathVariable Long id) {
        return permissionRepository.findById(id)
                .map(PermissionController::toDTO)
                .orElseThrow(() -> new NotFoundException("Permiso no encontrado"));
    }

    private static PermissionDTO toDTO(Permission permission) {
        return new PermissionDTO(permission.getId(), permission.getName().name(), permission.getDescription());
    }
}
