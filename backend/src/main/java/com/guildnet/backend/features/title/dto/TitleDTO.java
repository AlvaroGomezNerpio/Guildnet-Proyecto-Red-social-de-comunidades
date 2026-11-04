package com.guildnet.backend.features.title.dto;

import com.guildnet.backend.features.title.TitleMetric;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TitleDTO {

    private Long id;
    private String title;
    private String textColor;
    private String backgroundColor;
    private Long communityId;
    /** Regla automática (null si el título es manual). */
    private TitleMetric autoMetric;
    private Integer autoThreshold;

}

