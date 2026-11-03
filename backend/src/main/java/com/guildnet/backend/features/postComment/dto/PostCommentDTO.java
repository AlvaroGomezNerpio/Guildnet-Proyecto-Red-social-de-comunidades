package com.guildnet.backend.features.postComment.dto;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;

import java.time.LocalDateTime;

public record PostCommentDTO(
        Long id,
        String content,
        LocalDateTime createdAt,
        CommunityProfileDTO communityProfile
) {
}
