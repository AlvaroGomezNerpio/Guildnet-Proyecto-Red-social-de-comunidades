package com.guildnet.backend.features.title.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTitleRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 50, message = "El título no puede superar los 50 caracteres")
        String title,

        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color de texto no válido")
        String textColor,

        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color de fondo no válido")
        String backgroundColor,

        @NotNull(message = "La comunidad es obligatoria")
        Long communityId
) {
}
