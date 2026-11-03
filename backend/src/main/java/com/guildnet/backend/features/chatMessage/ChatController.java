package com.guildnet.backend.features.chatMessage;

import com.guildnet.backend.features.chatMessage.dto.ChatMessageDTO;
import com.guildnet.backend.features.chatMessage.dto.ChatMessageSendDTO;
import com.guildnet.backend.features.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** Historial del chat (REST). */
    @GetMapping("/api/v1/chat/{communityId}/messages")
    @ResponseBody
    public List<ChatMessageDTO> history(@PathVariable Long communityId, @AuthenticationPrincipal User user) {
        return chatService.getHistory(communityId, user);
    }

    /** Mensaje recibido por WebSocket: se guarda y se reenvía a todos los suscritos. */
    @MessageMapping("/chat/{communityId}")
    @SendTo("/topic/chat/{communityId}")
    public ChatMessageDTO send(@DestinationVariable Long communityId, @Payload ChatMessageSendDTO message, Principal principal) {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return chatService.send(communityId, message.content(), user);
    }
}
