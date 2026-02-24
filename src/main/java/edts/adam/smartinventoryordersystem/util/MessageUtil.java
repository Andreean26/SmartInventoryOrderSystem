package edts.adam.smartinventoryordersystem.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Centralized utility for resolving i18n messages from {@link MessageSource}.
 * <p>
 * Provides convenient methods for message resolution, eliminating the need
 * to inject {@code MessageSource} and handle locale resolution in every class.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * // Simple key lookup
 * messageUtil.get("product.created.success");
 *
 * // Key with arguments
 * messageUtil.get("product.not.found", productId);
 * </pre>
 */
@Component
public class MessageUtil {

    private final MessageSource messageSource;

    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    public String get(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }


    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
