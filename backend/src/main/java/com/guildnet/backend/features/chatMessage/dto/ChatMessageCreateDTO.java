package com.guildnet.backend.features.chatMessage.dto;

import lombok.Data;

@Data
public class ChatMessageCreateDTO {
    private String content;
    private Long communityId;
    private Long profileId;
}

