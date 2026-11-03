package com.guildnet.backend.features.profileComment.dto;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;

import java.time.LocalDateTime;

public record ProfileCommentDTO(
        Long id,
        String content,
        LocalDateTime createdAt,
        CommunityProfileDTO authorProfile,
        CommunityProfileDTO targetProfile
) {
}
