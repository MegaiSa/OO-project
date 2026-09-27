package fr.efrei.messaging.api.exception;

import java.time.Instant;
import java.util.List;

/** JSON body returned for every HTTP error. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, List.of());
    }
}
