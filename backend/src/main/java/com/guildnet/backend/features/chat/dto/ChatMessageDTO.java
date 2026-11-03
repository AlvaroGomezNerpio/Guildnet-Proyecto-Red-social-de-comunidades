package com.guildnet.backend.features.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageDTO(
        Long id,
        String content,
        LocalDateTime sentAt,
        Long communityId,
        Long profileId,
        String username,
        String profileImage
) {
}
