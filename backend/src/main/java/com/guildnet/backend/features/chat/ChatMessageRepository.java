package com.guildnet.backend.features.chat;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"community", "channel", "profile"})
    List<ChatMessage> findByChannelIdOrderBySentAtDesc(Long channelId, Limit limit);

    @Query("select max(m.id) from ChatMessage m where m.channel.id = :channelId")
    Optional<Long> findLastMessageId(@Param("channelId") Long channelId);

    /**
     * Mensajes nuevos por canal de una comunidad para un miembro: los que van después
     * del último que leyó, sin contar los suyos ni los anteriores a que se uniera.
     * Cada fila es {@code [channelId, count]}; los canales sin mensajes nuevos no aparecen.
     */
    @Query("""
            select m.channel.id, count(m) from ChatMessage m
            where m.channel.community.id = :communityId
              and m.profile.id <> :profileId
              and m.sentAt >= :since
              and m.id > coalesce((select r.lastReadMessageId from ChatChannelRead r
                                   where r.channelId = m.channel.id and r.profileId = :profileId), 0)
            group by m.channel.id
            """)
    List<Object[]> countUnreadByChannel(@Param("communityId") Long communityId,
                                        @Param("profileId") Long profileId,
                                        @Param("since") LocalDateTime since);
}
