package com.guildnet.backend.features.communityProfile.dto;

import com.guildnet.backend.features.role.dto.RoleDTO;
import com.guildnet.backend.features.title.Title;
import com.guildnet.backend.features.title.dto.TitleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityProfileDTO {
    private Long id;
    private String username;
    private String description;
    private String profileImage;
    private TitleDTO featuredTitle;
    private Long userId;
    private Long communityId;
    private List<RoleDTO> roles;
    private List<TitleDTO> titles;
}
