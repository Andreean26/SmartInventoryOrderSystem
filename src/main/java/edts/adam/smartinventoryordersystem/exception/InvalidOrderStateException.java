package edts.adam.smartinventoryordersystem.exception;

/** Thrown when an order status transition is not allowed (HTTP 400). */
public class InvalidOrderStateException extends LocalizedException {

    public InvalidOrderStateException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
