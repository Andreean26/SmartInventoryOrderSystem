package edts.adam.smartinventoryordersystem.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Configures internationalization (i18n) support.
 * Sets up MessageSource for property-based messages, locale resolution from
 * Accept-Language headers, and integrates i18n with Bean Validation.
 */
@Configuration
public class MessageConfig {

    /**
     * Configures MessageSource to load messages from classpath:messages*.properties
     * with UTF-8 encoding and caching.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }

    /**
     * Resolves locale from the Accept-Language HTTP header.
     * Defaults to English if no header is provided.
     * Supports: en, id (Indonesian).
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(List.of(Locale.ENGLISH, new Locale("id")));
        return resolver;
    }

    /**
     * Wires MessageSource into Bean Validation so that {key} syntax
     * in @NotBlank, @NotNull, etc. annotations resolves from messages.properties.
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }
}
