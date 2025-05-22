package com.guildnet.backend.features.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
    private List<String> tags;
}
