package edts.adam.smartinventoryordersystem.exception;

/** Thrown when a requested entity cannot be found (HTTP 404). */
public class ResourceNotFoundException extends LocalizedException {

    public ResourceNotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
