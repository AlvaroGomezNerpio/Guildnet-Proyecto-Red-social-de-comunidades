package com.guildnet.backend.features.profileComment.dto;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileCommentDTO {
    private Long id;
    private String content;
    private CommunityProfileDTO authorProfile;
    private CommunityProfileDTO targetProfile;
}