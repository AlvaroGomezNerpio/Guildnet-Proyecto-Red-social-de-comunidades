package com.guildnet.backend.features.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
}
