package com.guildnet.backend.features.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleDTO(
        Long id,

        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 50, message = "El nombre del rol no puede superar los 50 caracteres")
        String name,

        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color de texto no válido")
        String textColor,

        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color de fondo no válido")
        String backgroundColor,

        Long communityId
) {
}
