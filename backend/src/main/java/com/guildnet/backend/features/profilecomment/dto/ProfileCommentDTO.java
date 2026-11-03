package com.guildnet.backend.features.profilecomment.dto;

import com.guildnet.backend.features.profile.dto.CommunityProfileDTO;

import java.time.LocalDateTime;

public record ProfileCommentDTO(
        Long id,
        String content,
        LocalDateTime createdAt,
        CommunityProfileDTO authorProfile,
        CommunityProfileDTO targetProfile
) {
}
