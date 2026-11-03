package com.guildnet.backend.features.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateUserTagsRequest(
        @NotNull
        @Size(max = 20, message = "No puedes tener más de 20 etiquetas")
        List<@Size(max = 30) String> tags
) {
}
