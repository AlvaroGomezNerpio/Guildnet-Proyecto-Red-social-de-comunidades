package com.guildnet.backend.features.role.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDTO {
    private Long id;
    private String name;
    private String textColor;
    private String backgroundColor;
    private Long communityId;
}
