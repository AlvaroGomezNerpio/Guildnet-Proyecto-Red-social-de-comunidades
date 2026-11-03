package com.guildnet.backend.features.user.dto;

import java.util.List;

public record UserDTO(
        Long id,
        String username,
        String email,
        String profileImage,
        List<String> tags
) {
}
