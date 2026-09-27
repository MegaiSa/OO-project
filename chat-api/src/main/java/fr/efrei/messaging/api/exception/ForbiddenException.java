package fr.efrei.messaging.api.exception;

/** Mapped to HTTP 403 by {@link GlobalExceptionHandler}. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
