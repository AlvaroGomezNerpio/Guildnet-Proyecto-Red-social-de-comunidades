package com.guildnet.backend.features.chat.dto;

/** Mensaje que envía el cliente por WebSocket. */
public record ChatMessageSendDTO(String content) {
}
