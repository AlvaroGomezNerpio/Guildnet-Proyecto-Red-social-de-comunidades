package com.guildnet.backend.features.community.dto;

import java.util.List;

public record CommunityResponseDTO(
        Long id,
        String name,
        String description,
        String rules,
        String image,
        String banner,
        String primaryColor,
        String accentColor,
        List<String> tags,
        Long ownerId
) {
}
