package edts.adam.smartinventoryordersystem.exception;

/**
 * Thrown when the requested quantity exceeds available product stock (HTTP
 * 400).
 */
public class InsufficientStockException extends LocalizedException {

    public InsufficientStockException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
