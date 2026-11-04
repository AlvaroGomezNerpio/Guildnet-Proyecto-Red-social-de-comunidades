package com.guildnet.backend.features.chat;

import com.guildnet.backend.features.chat.dto.ChatChannelDTO;
import com.guildnet.backend.features.chat.dto.ChatMessageDTO;
import com.guildnet.backend.features.chat.dto.ChatMessageSendDTO;
import com.guildnet.backend.features.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ------------------------------------------------------------------ Canales (REST)

    @GetMapping("/api/v1/communities/{communityId}/channels")
    @ResponseBody
    public List<ChatChannelDTO> channels(@PathVariable Long communityId, @AuthenticationPrincipal User user) {
        return chatService.getChannels(communityId, user);
    }

    @PostMapping("/api/v1/communities/{communityId}/channels")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ChatChannelDTO createChannel(@PathVariable Long communityId, @RequestBody @Valid ChatChannelDTO dto,
                                        @AuthenticationPrincipal User user) {
        return chatService.createChannel(communityId, dto, user);
    }

    @PutMapping("/api/v1/channels/{channelId}")
    @ResponseBody
    public ChatChannelDTO updateChannel(@PathVariable Long channelId, @RequestBody @Valid ChatChannelDTO dto,
                                        @AuthenticationPrincipal User user) {
        return chatService.updateChannel(channelId, dto, user);
    }

    @DeleteMapping("/api/v1/channels/{channelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChannel(@PathVariable Long channelId, @AuthenticationPrincipal User user) {
        chatService.deleteChannel(channelId, user);
    }

    /** Marca el canal como leído (al abrirlo o al recibir mensajes con él abierto). */
    @PutMapping("/api/v1/channels/{channelId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long channelId, @AuthenticationPrincipal User user) {
        chatService.markRead(channelId, user);
    }

    // ------------------------------------------------------------------ Mensajes

    /** Historial del canal (REST). */
    @GetMapping("/api/v1/channels/{channelId}/messages")
    @ResponseBody
    public List<ChatMessageDTO> history(@PathVariable Long channelId, @AuthenticationPrincipal User user) {
        return chatService.getHistory(channelId, user);
    }

    /** Mensaje recibido por WebSocket: se guarda y se reenvía a todos los suscritos al canal. */
    @MessageMapping("/chat/channel/{channelId}")
    @SendTo("/topic/chat/channel/{channelId}")
    public ChatMessageDTO send(@DestinationVariable Long channelId, @Payload ChatMessageSendDTO message, Principal principal) {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return chatService.send(channelId, message.content(), user);
    }
}
