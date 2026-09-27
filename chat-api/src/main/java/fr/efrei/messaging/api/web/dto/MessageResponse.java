package fr.efrei.messaging.api.web.dto;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderUsername,
        String content,
        Instant sentAt) {
}
