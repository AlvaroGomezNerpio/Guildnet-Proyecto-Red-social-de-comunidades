package com.guildnet.backend.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Todos los campos son opcionales: solo se actualizan los que vienen informados. */
public record UpdateUserRequest(
        @Size(min = 1, max = 50, message = "El nombre de usuario debe tener entre 1 y 50 caracteres")
        String username,

        @Email(message = "El email no es válido")
        String email,

        @Size(max = 20, message = "No puedes tener más de 20 etiquetas")
        List<@Size(max = 30) String> tags
) {
}
