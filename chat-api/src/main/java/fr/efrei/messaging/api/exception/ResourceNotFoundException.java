package fr.efrei.messaging.api.exception;

/** Mapped to HTTP 404 by {@link GlobalExceptionHandler}. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " with id " + id + " not found");
    }
}
