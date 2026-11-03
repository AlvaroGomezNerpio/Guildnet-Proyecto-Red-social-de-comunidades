package com.guildnet.backend.features.profile.dto;

import jakarta.validation.constraints.Size;

/** Campos opcionales: solo se actualizan los que vienen informados. */
public record UpdateCommunityProfileRequest(
        @Size(min = 1, max = 50, message = "El nombre debe tener entre 1 y 50 caracteres")
        String userName,

        String description
) {
}
