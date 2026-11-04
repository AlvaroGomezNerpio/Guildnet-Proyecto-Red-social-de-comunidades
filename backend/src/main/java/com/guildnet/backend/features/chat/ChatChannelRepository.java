package com.guildnet.backend.features.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {

    List<ChatChannel> findByCommunityIdOrderByIdAsc(Long communityId);

    long countByCommunityId(Long communityId);

    boolean existsByCommunityIdAndNameIgnoreCase(Long communityId, String name);

    boolean existsByCommunityIdAndNameIgnoreCaseAndIdNot(Long communityId, String name, Long id);

    /** Comunidad del canal, sin cargar la entidad (para autorizar suscripciones WebSocket). */
    @Query("select c.community.id from ChatChannel c where c.id = :id")
    Optional<Long> findCommunityIdById(@Param("id") Long id);
}
