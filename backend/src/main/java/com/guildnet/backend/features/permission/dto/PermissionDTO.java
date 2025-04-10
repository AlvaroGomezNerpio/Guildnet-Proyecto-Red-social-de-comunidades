package com.guildnet.backend.features.permission.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDTO {
    private Long id;
    private String name;
    private String description;
}
