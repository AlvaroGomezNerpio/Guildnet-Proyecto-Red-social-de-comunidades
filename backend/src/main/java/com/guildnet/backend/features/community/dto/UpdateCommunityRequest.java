package com.guildnet.backend.features.community.dto;

import com.guildnet.backend.features.community.CommunityColors;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCommunityRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        String description,

        String rules,

        @Size(max = 20, message = "No puedes añadir más de 20 etiquetas")
        List<@Size(max = 30) String> tags,

        /** Opcional: si no se indica se usa (o se mantiene) el color actual. */
        @Pattern(regexp = CommunityColors.HEX_PATTERN, message = "El color principal debe tener el formato #rrggbb")
        String primaryColor,

        @Pattern(regexp = CommunityColors.HEX_PATTERN, message = "El color de acento debe tener el formato #rrggbb")
        String accentColor,

        @Pattern(regexp = CommunityColors.HEADING_FONT_PATTERN, message = "Tipografía no válida")
        String headingFont,

        @Size(max = 30, message = "El nombre de los miembros no puede superar los 30 caracteres")
        String membersLabel,

        @Size(max = 255, message = "El mensaje de bienvenida no puede superar los 255 caracteres")
        String welcomeMessage,

        Boolean requireRulesAcceptance,

        @Size(max = 6, message = "No puedes añadir más de 6 enlaces")
        List<@Valid CommunityLinkDTO> links
) {
}
