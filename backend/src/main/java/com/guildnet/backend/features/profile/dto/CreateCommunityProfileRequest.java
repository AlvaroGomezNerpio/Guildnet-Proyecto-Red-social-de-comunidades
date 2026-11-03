package com.guildnet.backend.features.profile.dto;

import jakarta.validation.constraints.Size;

public record CreateCommunityProfileRequest(
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String username,

        String description
) {
}
