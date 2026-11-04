package com.guildnet.backend.features.title.dto;

import com.guildnet.backend.features.title.TitleMetric;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Regla de un título automático: se consigue al llegar a {@code threshold} en la métrica. */
public record TitleRuleRequest(
        @NotNull(message = "Indica qué actividad se mide")
        TitleMetric metric,

        @NotNull(message = "Indica la cantidad necesaria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @Max(value = 100000, message = "La cantidad no puede superar 100.000")
        Integer threshold
) {
}
