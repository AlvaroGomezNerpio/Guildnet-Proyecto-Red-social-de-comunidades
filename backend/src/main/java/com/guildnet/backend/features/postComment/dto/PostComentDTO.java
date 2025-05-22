package com.guildnet.backend.features.postComment.dto;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostComentDTO {
    private Long id;
    private String content;
    private CommunityProfileDTO communityProfile;
}
