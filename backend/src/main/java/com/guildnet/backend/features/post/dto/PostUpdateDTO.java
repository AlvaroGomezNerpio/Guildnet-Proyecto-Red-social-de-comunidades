package com.guildnet.backend.features.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpdateDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @NotBlank(message = "El contenido es obligatorio")
        String content,

        @Size(max = 10, message = "No puedes añadir más de 10 etiquetas")
        List<@Size(max = 30) String> tags,

        /** Categoría de la comunidad (opcional). */
        Long categoryId
) {
}
