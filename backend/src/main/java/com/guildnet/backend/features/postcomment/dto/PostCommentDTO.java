package com.guildnet.backend.features.postcomment.dto;

import com.guildnet.backend.features.profile.dto.CommunityProfileDTO;

import java.time.LocalDateTime;

public record PostCommentDTO(
        Long id,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CommunityProfileDTO communityProfile
) {
}
