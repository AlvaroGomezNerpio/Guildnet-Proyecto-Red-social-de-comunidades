package com.guildnet.backend.features.postComment.dto;

import lombok.Data;

@Data
public class PostCommentCreateDTO {
    private Long postId;
    private Long profileId;
    private String content;
}