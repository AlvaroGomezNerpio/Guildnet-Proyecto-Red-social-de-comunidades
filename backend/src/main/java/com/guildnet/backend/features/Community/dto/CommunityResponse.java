package com.guildnet.backend.features.Community.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {
    private Long id;
    private String name;
    private String description;
    private String rules;
    private String image;
    private String banner;
    private List<String> tags;
    private Long ownerId;
}
