package com.guildnet.backend.features.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Enlace de la comunidad. Solo se admiten direcciones http(s), nunca javascript: ni similares. */
public record CommunityLinkDTO(
        @NotBlank(message = "Cada enlace necesita un nombre")
        @Size(max = 40, message = "El nombre del enlace no puede superar los 40 caracteres")
        String label,

        @NotBlank(message = "Cada enlace necesita una dirección")
        @Size(max = 500, message = "La dirección del enlace es demasiado larga")
        @Pattern(regexp = "^https?://[^\\s]+$", message = "La dirección del enlace debe empezar por http:// o https://")
        String url
) {
}
