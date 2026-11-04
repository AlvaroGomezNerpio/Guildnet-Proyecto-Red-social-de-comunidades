package com.guildnet.backend.features.chat;

import com.guildnet.backend.common.exception.BadRequestException;
import com.guildnet.backend.features.chat.dto.ChatMessageDTO;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    static final int MAX_LENGTH = 2000;
    private static final int HISTORY_SIZE = 100;

    private final ChatMessageRepository chatMessageRepository;
    private final AccessGuard accessGuard;

    /** Últimos mensajes del chat de la comunidad, en orden cronológico. Solo para miembros. */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getHistory(Long communityId, User user) {
        accessGuard.requireMembership(user, communityId);
        List<ChatMessage> latest = new ArrayList<>(
                chatMessageRepository.findByCommunityIdOrderBySentAtDesc(communityId, Limit.of(HISTORY_SIZE)));
        Collections.reverse(latest);
        return latest.stream().map(ChatService::toDTO).toList();
    }

    /** Guarda el mensaje enviado por el usuario como su perfil en la comunidad. */
    public ChatMessageDTO send(Long communityId, String content, User user) {
        CommunityProfile profile = accessGuard.requireMembership(user, communityId);
        String text = content == null ? "" : content.trim();
        if (text.isEmpty() || text.length() > MAX_LENGTH) {
            throw new BadRequestException("El mensaje debe tener entre 1 y " + MAX_LENGTH + " caracteres");
        }

        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .content(text)
                .community(profile.getCommunity())
                .profile(profile)
                .build());
        return toDTO(message);
    }

    private static ChatMessageDTO toDTO(ChatMessage message) {
        return new ChatMessageDTO(
                message.getId(),
                message.getContent(),
                message.getSentAt(),
                message.getCommunity().getId(),
                message.getProfile().getId(),
                message.getProfile().getUsername(),
                message.getProfile().getProfileImage()
        );
    }
}
