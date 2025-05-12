package com.guildnet.backend.features.chatMessage;

import com.guildnet.backend.features.chatMessage.dto.ChatMessageCreateDTO;
import com.guildnet.backend.features.chatMessage.dto.ChatMessageDTO;

import java.security.Principal;
import java.util.List;

public interface ChatMessageService {
    List<ChatMessageDTO> getMessagesByCommunity(Long communityId);
    ChatMessageDTO saveMessage(ChatMessageCreateDTO dto);
    ChatMessageDTO processIncomingMessage(Long communityId, ChatMessageDTO messageDTO, Principal principal);
}


