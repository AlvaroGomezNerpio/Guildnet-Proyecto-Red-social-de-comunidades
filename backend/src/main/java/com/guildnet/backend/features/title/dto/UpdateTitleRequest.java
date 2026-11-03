package com.guildnet.backend.features.title.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Campos opcionales: solo se actualizan los que vienen informados. */
public record UpdateTitleRequest(
        @Size(min = 1, max = 50, message = "El título debe tener entre 1 y 50 caracteres")
        String title,

        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color de texto no válido")
        String textColor,

        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color de fondo no válido")
        String backgroundColor
) {
}
