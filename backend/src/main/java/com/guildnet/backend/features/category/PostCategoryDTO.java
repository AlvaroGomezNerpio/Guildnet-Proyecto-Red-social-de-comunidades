package com.guildnet.backend.features.category;

import com.guildnet.backend.features.community.CommunityColors;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Categoría de publicaciones. Al crear o editar, los colores son opcionales:
 * sin ellos se usa el color de la comunidad con un texto legible encima.
 */
public record PostCategoryDTO(
        Long id,

        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 30, message = "El nombre de la categoría no puede superar los 30 caracteres")
        String name,

        @Pattern(regexp = CommunityColors.HEX_PATTERN, message = "Color de texto no válido")
        String textColor,

        @Pattern(regexp = CommunityColors.HEX_PATTERN, message = "Color de fondo no válido")
        String backgroundColor,

        Long communityId
) {

    public static PostCategoryDTO of(PostCategory category) {
        return category == null ? null : new PostCategoryDTO(
                category.getId(),
                category.getName(),
                category.getTextColor(),
                category.getBackgroundColor(),
                category.getCommunity().getId());
    }
}
