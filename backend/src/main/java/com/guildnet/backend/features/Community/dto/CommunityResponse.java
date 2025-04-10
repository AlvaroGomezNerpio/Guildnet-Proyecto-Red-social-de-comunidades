package com.guildnet.backend.features.Community.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {
    private Long id;           // ID de la comunidad
    private String name;       // Nombre
    private String description;// Descripción
    private String rules;      // Reglas
    private String image;      // URL de la imagen
    private String banner;     // URL del banner
    private List<String> tags;
    private Long ownerId;  // Nombre del creador
}
