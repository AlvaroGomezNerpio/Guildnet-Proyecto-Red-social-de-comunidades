package com.guildnet.backend.features.post.dto;

import com.guildnet.backend.features.communityProfile.dto.CommunityProfileDTO;
import com.guildnet.backend.features.postComment.dto.PostComentDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailDTO {
    private Long id;
    private String title;
    private String content;
    private List<String> tags;
    private List<PostComentDTO> postComment;
    private CommunityProfileDTO communityProfile;
    private Long communityId;
}
