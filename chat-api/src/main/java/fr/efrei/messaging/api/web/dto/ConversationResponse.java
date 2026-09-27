package fr.efrei.messaging.api.web.dto;

import java.time.Instant;
import java.util.List;

public record ConversationResponse(
        Long id,
        String name,
        List<UserResponse> participants,
        Long messageCount, // null if message-server is unavailable
        Instant createdAt) {
}
