package com.guildnet.backend.features.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatChannelReadRepository extends JpaRepository<ChatChannelRead, ChatChannelRead.Key> {
}
