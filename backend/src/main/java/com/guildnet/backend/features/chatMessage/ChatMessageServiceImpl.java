package com.guildnet.backend.features.chatMessage;

import com.guildnet.backend.features.Community.Community;
import com.guildnet.backend.features.Community.CommunityRepository;
import com.guildnet.backend.features.chatMessage.dto.ChatMessageCreateDTO;
import com.guildnet.backend.features.chatMessage.dto.ChatMessageDTO;
import com.guildnet.backend.features.communityProfile.CommunityProfile;
import com.guildnet.backend.features.communityProfile.CommunityProfileRepository;
import com.guildnet.backend.security.jwt.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final CommunityRepository communityRepository;
    private final CommunityProfileRepository profileRepository;

    @Override
    public List<ChatMessageDTO> getMessagesByCommunity(Long communityId) {
        return chatMessageRepository.findByCommunityIdOrderBySentAtAsc(communityId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ChatMessageDTO saveMessage(ChatMessageCreateDTO dto) {
        Community community = communityRepository.findById(dto.getCommunityId())
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        CommunityProfile profile = profileRepository.findById(dto.getProfileId())
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        ChatMessage message = ChatMessage.builder()
                .community(community)
                .profile(profile)
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .build();

        return toDTO(chatMessageRepository.save(message));
    }

    @Override
    public ChatMessageDTO processIncomingMessage(Long communityId, ChatMessageDTO dto, Principal principal) {
        CommunityProfile profile = profileRepository.findByUserUsernameAndCommunityId(principal.getName(), communityId)
                .orElseThrow(() -> new RuntimeException("El usuario no pertenece a la comunidad"));

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Comunidad no encontrada"));

        ChatMessage message = ChatMessage.builder()
                .content(dto.getContent())
                .sentAt(LocalDateTime.now())
                .community(community)
                .profile(profile)
                .build();

        return toDTO(chatMessageRepository.save(message));
    }

    private ChatMessageDTO toDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .communityId(message.getCommunity().getId())
                .profileId(message.getProfile().getId())
                .username(message.getProfile().getUsername())
                .profileImage(message.getProfile().getProfileImage())
                .build();
    }
}



