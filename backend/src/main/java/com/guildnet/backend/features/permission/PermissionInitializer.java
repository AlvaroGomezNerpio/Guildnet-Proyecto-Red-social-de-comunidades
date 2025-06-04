package com.guildnet.backend.features.permission;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PermissionInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    public PermissionInitializer(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) {
        for (PermissionType type : PermissionType.values()) {
            if (!permissionRepository.existsByName(type)) {
                Permission permission = new Permission();
                permission.setName(type);
                permission.setDescription(generateDescription(type));
                permissionRepository.save(permission);
            }
        }
    }

    private String generateDescription(PermissionType type) {
        return switch (type) {
            case EDIT_POST -> "Poder editar post de otros usuarios";
            case DELETE_POST -> "Poder borrar cualquier post";
            case EDIT_COMMENTS -> "Poder editar comentarios de otros usuarios";
            case DELETE_COMMENTS -> "Poder borrar cualquier comentario";
            case ASSIGN_ROLES -> "Poder crear y asignar roles a usuarios";
            case ASSIGN_TITLES -> "Poder crear y asignar títulos a usuarios";
            case BAN_USER -> "Poder eliminar usuarios de la comunidad";
            case MANAGE_COMMUNITY_SETTINGS -> "Poder editar la comunidad";
        };
    }
}

