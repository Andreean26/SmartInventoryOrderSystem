package edts.adam.smartinventoryordersystem.exception;

/** Thrown when a request violates a business rule (HTTP 400). */
public class BadRequestException extends LocalizedException {

    public BadRequestException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
