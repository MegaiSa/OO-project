package fr.efrei.messaging.api.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateConversationRequest(
        @NotBlank
        @Size(max = 80)
        String name,

        @NotNull
        @Size(min = 2, message = "a conversation needs at least 2 participants")
        List<Long> participantIds) {
}
