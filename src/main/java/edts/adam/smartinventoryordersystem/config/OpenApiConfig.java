package edts.adam.smartinventoryordersystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures OpenAPI/Swagger documentation metadata including
 * API title, description, version, and contact information.
 */
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI smartInventoryOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Smart Inventory & Order Management API")
                                                .description("REST API for managing products, customers, and orders "
                                                                + "with membership-based discount rules.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Adam uhuyy")
                                                                .email("fawazzakyadam26@gmail.com")));
        }
}
