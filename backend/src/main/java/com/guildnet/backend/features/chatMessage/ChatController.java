package com.guildnet.backend.features.chatMessage;

import com.guildnet.backend.features.chatMessage.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/{communityId}")
    @SendTo("/topic/chat/{communityId}")
    public ChatMessageDTO sendMessage(@DestinationVariable Long communityId,
                                      ChatMessageDTO messageDTO,
                                      Principal principal) {
        return chatMessageService.processIncomingMessage(communityId, messageDTO, principal);
    }
}


