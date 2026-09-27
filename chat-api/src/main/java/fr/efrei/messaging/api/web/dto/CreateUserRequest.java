package fr.efrei.messaging.api.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "only letters, digits, '_', '.' and '-' are allowed")
        String username,

        @NotBlank
        @Size(max = 60)
        String displayName) {
}
