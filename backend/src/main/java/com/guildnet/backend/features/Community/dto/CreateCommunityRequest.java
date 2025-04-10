package com.guildnet.backend.features.Community.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateCommunityRequest {
    private String name;         // nombre de la comunidad
    private String description;  // descripción
    private String rules;        // reglas de la comunidad
    private List<String> tags;
}
