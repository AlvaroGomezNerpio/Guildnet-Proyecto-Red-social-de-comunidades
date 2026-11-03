package com.guildnet.backend.features.post.dto;

import com.guildnet.backend.features.profile.dto.CommunityProfileDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private List<String> tags;
    private int likes;
    private int comments;
    private LocalDateTime createdAt;
    private CommunityProfileDTO communityProfile;
    private Long communityId;
}
