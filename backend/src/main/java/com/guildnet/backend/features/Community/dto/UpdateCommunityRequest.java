package com.guildnet.backend.features.Community.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateCommunityRequest {
    private String name;
    private String description;
    private String rules;
    private List<String> tags;
}
