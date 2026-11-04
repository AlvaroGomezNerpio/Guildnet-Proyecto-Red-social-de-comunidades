package com.guildnet.backend.features.post.dto;

import com.guildnet.backend.features.profile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.postcomment.dto.PostCommentDTO;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailDTO(
        Long id,
        String title,
        String content,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PostCommentDTO> postComment,
        CommunityProfileDTO communityProfile,
        Long communityId,
        boolean pinned
) {
}
