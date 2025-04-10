package com.guildnet.backend.features.communityProfile.dto;

import lombok.Data;

@Data
public class UpdateCommunityProfileRequest {
    private String userName;
    private String description;
}
