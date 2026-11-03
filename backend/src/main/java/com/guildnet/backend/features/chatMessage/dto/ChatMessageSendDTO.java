package com.guildnet.backend.features.chatMessage.dto;

/** Mensaje que envía el cliente por WebSocket. */
public record ChatMessageSendDTO(String content) {
}
