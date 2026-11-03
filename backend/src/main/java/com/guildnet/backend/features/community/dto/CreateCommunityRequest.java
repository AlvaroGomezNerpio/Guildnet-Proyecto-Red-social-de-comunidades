package com.guildnet.backend.features.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCommunityRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        String description,

        String rules,

        @Size(max = 20, message = "No puedes añadir más de 20 etiquetas")
        List<@Size(max = 30) String> tags
) {
}
