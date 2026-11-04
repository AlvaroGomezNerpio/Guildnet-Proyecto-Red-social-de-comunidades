package com.guildnet.backend.features.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Canal de chat. Al crear o editar solo se usan {@code name} y {@code description}. */
public record ChatChannelDTO(
        Long id,

        @NotBlank(message = "El nombre del canal es obligatorio")
        @Size(max = 40, message = "El nombre del canal no puede superar los 40 caracteres")
        @Pattern(regexp = "^[\\p{L}\\p{N} _-]+$", message = "El nombre solo puede tener letras, números, espacios, guiones y guiones bajos")
        String name,

        @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
        String description,

        Long communityId
) {
}
