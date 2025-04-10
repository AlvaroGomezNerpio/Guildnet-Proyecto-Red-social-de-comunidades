package com.guildnet.backend.features.title.dto;

import lombok.Data;

@Data
public class UpdateTitleRequest {
    private String title;
    private String textColor;
    private String backgroundColor;
}

