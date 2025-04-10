package com.guildnet.backend.features.communityProfile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommunityProfileRequest {
    private String username;
    private String description;
}
