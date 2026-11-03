package com.guildnet.backend.features.chat;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"community", "profile"})
    List<ChatMessage> findByCommunityIdOrderBySentAtDesc(Long communityId, Limit limit);
}
