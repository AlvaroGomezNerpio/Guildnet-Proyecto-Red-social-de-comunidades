package com.guildnet.backend.features.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Datos para crear una publicación. El autor es siempre el perfil del usuario
 * autenticado en la comunidad indicada; {@code profileId} se mantiene por compatibilidad.
 */
public record PostCreateDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @NotBlank(message = "El contenido es obligatorio")
        String content,

        @Size(max = 10, message = "No puedes añadir más de 10 etiquetas")
        List<@Size(max = 30) String> tags,

        Long profileId,

        @NotNull(message = "La comunidad es obligatoria")
        Long communityId
) {
}
