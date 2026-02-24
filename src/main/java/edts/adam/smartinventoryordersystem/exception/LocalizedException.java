package edts.adam.smartinventoryordersystem.exception;

/**
 * Abstract base exception carrying an i18n message key and arguments.
 * Subclasses represent specific business errors resolved by
 * {@link GlobalExceptionHandler}.
 */
public abstract class LocalizedException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    protected LocalizedException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
