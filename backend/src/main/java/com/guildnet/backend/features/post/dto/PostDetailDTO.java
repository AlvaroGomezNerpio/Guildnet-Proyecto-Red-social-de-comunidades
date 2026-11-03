package com.guildnet.backend.features.post.dto;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.postComment.dto.PostCommentDTO;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailDTO(
        Long id,
        String title,
        String content,
        List<String> tags,
        LocalDateTime createdAt,
        List<PostCommentDTO> postComment,
        CommunityProfileDTO communityProfile,
        Long communityId
) {
}
