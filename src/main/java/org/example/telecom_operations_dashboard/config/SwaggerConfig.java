package org.example.telecom_operations_dashboard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for the Telecom Operations Dashboard API.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Telecom Operations Dashboard API")
                        .description("API documentation for the Telecom Operations Dashboard application. " +
                                "This API provides endpoints for traffic monitoring, alerts management, and data streaming.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Telecom Operations Team")
                                .email("support@telecom.com")));
    }
}
