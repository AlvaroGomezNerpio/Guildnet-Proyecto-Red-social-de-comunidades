package com.guildnet.backend.features.title.dto;

import lombok.Data;

@Data
public class CreateTitleRequest {
    private String title;
    private String textColor;
    private String backgroundColor;
    private Long communityId;
}
