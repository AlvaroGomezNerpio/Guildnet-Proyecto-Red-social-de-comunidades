package com.guildnet.backend.features.chat;

import com.guildnet.backend.common.exception.BadRequestException;
import com.guildnet.backend.common.exception.ConflictException;
import com.guildnet.backend.common.exception.NotFoundException;
import com.guildnet.backend.features.chat.dto.ChatChannelDTO;
import com.guildnet.backend.features.chat.dto.ChatMessageDTO;
import com.guildnet.backend.features.community.Community;
import com.guildnet.backend.features.community.CommunityRepository;
import com.guildnet.backend.features.permission.PermissionType;
import com.guildnet.backend.features.profile.CommunityProfile;
import com.guildnet.backend.features.user.User;
import com.guildnet.backend.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chat de las comunidades, organizado en canales. Cada comunidad crea los
 * canales que quiere (no hay ninguno por defecto); los gestiona quien tenga
 * MANAGE_CHANNELS. Leer y escribir: cualquier miembro.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    static final int MAX_LENGTH = 2000;
    static final int MAX_CHANNELS = 20;
    private static final int HISTORY_SIZE = 100;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatChannelRepository channelRepository;
    private final ChatChannelReadRepository readRepository;
    private final CommunityRepository communityRepository;
    private final AccessGuard accessGuard;

    // ------------------------------------------------------------------ Canales

    /** Canales de la comunidad con los mensajes nuevos de cada uno para quien consulta. */
    @Transactional(readOnly = true)
    public List<ChatChannelDTO> getChannels(Long communityId, User user) {
        CommunityProfile me = accessGuard.requireMembership(user, communityId);
        LocalDateTime since = me.getJoinedAt() != null ? me.getJoinedAt() : LocalDateTime.MIN;
        Map<Long, Integer> unread = new HashMap<>();
        for (Object[] row : chatMessageRepository.countUnreadByChannel(communityId, me.getId(), since)) {
            unread.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return channelRepository.findByCommunityIdOrderByIdAsc(communityId).stream()
                .map(channel -> toDTO(channel, unread.getOrDefault(channel.getId(), 0)))
                .toList();
    }

    /** Marca como leídos todos los mensajes actuales del canal para el usuario. */
    public void markRead(Long channelId, User user) {
        ChatChannel channel = loadChannel(channelId);
        CommunityProfile me = accessGuard.requireMembership(user, channel.getCommunity().getId());
        Long last = chatMessageRepository.findLastMessageId(channelId).orElse(0L);
        ChatChannelRead read = readRepository.findById(new ChatChannelRead.Key(me.getId(), channelId))
                .orElseGet(() -> new ChatChannelRead(me.getId(), channelId, 0L));
        if (last > read.getLastReadMessageId()) {
            read.setLastReadMessageId(last);
            readRepository.save(read);
        }
    }

    public ChatChannelDTO createChannel(Long communityId, ChatChannelDTO dto, User user) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Comunidad no encontrada"));
        accessGuard.requirePermission(user, communityId, PermissionType.MANAGE_CHANNELS);

        String name = dto.name().trim();
        if (channelRepository.existsByCommunityIdAndNameIgnoreCase(communityId, name)) {
            throw new ConflictException("Ya existe un canal con ese nombre");
        }
        if (channelRepository.countByCommunityId(communityId) >= MAX_CHANNELS) {
            throw new ConflictException("Una comunidad puede tener como mucho " + MAX_CHANNELS + " canales");
        }
        ChatChannel channel = channelRepository.save(ChatChannel.builder()
                .name(name)
                .description(blankToNull(dto.description()))
                .community(community)
                .build());
        return toDTO(channel);
    }

    public ChatChannelDTO updateChannel(Long channelId, ChatChannelDTO dto, User user) {
        ChatChannel channel = loadChannel(channelId);
        Long communityId = channel.getCommunity().getId();
        accessGuard.requirePermission(user, communityId, PermissionType.MANAGE_CHANNELS);

        String name = dto.name().trim();
        if (channelRepository.existsByCommunityIdAndNameIgnoreCaseAndIdNot(communityId, name, channelId)) {
            throw new ConflictException("Ya existe un canal con ese nombre");
        }
        channel.setName(name);
        channel.setDescription(blankToNull(dto.description()));
        return toDTO(channel);
    }

    /** Borra el canal y todos sus mensajes. */
    public void deleteChannel(Long channelId, User user) {
        ChatChannel channel = loadChannel(channelId);
        accessGuard.requirePermission(user, channel.getCommunity().getId(), PermissionType.MANAGE_CHANNELS);
        channelRepository.delete(channel);
    }

    // ------------------------------------------------------------------ Mensajes

    /** Últimos mensajes del canal, en orden cronológico. Solo para miembros. */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getHistory(Long channelId, User user) {
        ChatChannel channel = loadChannel(channelId);
        accessGuard.requireMembership(user, channel.getCommunity().getId());
        List<ChatMessage> latest = new ArrayList<>(
                chatMessageRepository.findByChannelIdOrderBySentAtDesc(channelId, Limit.of(HISTORY_SIZE)));
        Collections.reverse(latest);
        return latest.stream().map(ChatService::toDTO).toList();
    }

    /** Guarda el mensaje enviado por el usuario como su perfil en la comunidad del canal. */
    public ChatMessageDTO send(Long channelId, String content, User user) {
        ChatChannel channel = loadChannel(channelId);
        CommunityProfile profile = accessGuard.requireMembership(user, channel.getCommunity().getId());
        String text = content == null ? "" : content.trim();
        if (text.isEmpty() || text.length() > MAX_LENGTH) {
            throw new BadRequestException("El mensaje debe tener entre 1 y " + MAX_LENGTH + " caracteres");
        }

        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .content(text)
                .channel(channel)
                .community(channel.getCommunity())
                .profile(profile)
                .build());
        return toDTO(message);
    }

    private ChatChannel loadChannel(Long id) {
        return channelRepository.findById(id).orElseThrow(() -> new NotFoundException("Canal no encontrado"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ChatChannelDTO toDTO(ChatChannel channel) {
        return toDTO(channel, 0);
    }

    private static ChatChannelDTO toDTO(ChatChannel channel, int unread) {
        return new ChatChannelDTO(channel.getId(), channel.getName(), channel.getDescription(),
                channel.getCommunity().getId(), unread);
    }

    private static ChatMessageDTO toDTO(ChatMessage message) {
        return new ChatMessageDTO(
                message.getId(),
                message.getContent(),
                message.getSentAt(),
                message.getCommunity().getId(),
                message.getChannel().getId(),
                message.getProfile().getId(),
                message.getProfile().getUsername(),
                message.getProfile().getProfileImage()
        );
    }
}
