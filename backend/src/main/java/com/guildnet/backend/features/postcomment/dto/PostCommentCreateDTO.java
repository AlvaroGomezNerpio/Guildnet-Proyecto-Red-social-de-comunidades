package com.guildnet.backend.features.postcomment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code profileId} se mantiene por compatibilidad: el autor es siempre el perfil del usuario autenticado. */
public record PostCommentCreateDTO(
        @NotNull Long postId,
        Long profileId,
        @NotBlank(message = "El comentario no puede estar vacío")
        @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
        String content
) {
}
