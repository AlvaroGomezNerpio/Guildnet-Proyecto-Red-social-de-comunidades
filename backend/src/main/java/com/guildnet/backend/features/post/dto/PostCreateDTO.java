package com.guildnet.backend.features.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDTO {
    private String title;
    private String content;
    private List<String> tags;
    private Long profileId; // ID del perfil que crea la publicación
}
