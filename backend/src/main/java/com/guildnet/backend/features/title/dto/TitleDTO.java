package com.guildnet.backend.features.title.dto;

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

}

