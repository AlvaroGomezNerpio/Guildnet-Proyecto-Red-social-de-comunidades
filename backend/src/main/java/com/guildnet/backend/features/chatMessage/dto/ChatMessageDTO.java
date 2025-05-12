package com.guildnet.backend.features.chatMessage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String content;
    private LocalDateTime sentAt;
    private Long communityId;
    private Long profileId;
    private String username;
    private String profileImage;
}


