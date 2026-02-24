package edts.adam.smartinventoryordersystem.exception;

/**
 * Thrown when attempting to create a resource that already exists (HTTP 409).
 */
public class DuplicateResourceException extends LocalizedException {

    public DuplicateResourceException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
