package com.guildnet.backend.features.Community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String rules;
    private String image;
    private String banner;
    private List<String> tags;
}
