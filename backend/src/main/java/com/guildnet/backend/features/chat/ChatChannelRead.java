package com.guildnet.backend.features.chat;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/** Hasta qué mensaje ha leído un miembro en un canal (para los contadores de mensajes nuevos). */
@Entity
@Table(name = "chat_channel_reads")
@IdClass(ChatChannelRead.Key.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatChannelRead {

    @Id
    @Column(name = "profile_id")
    private Long profileId;

    @Id
    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "last_read_message_id", nullable = false)
    private Long lastReadMessageId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {
        private Long profileId;
        private Long channelId;
    }
}
