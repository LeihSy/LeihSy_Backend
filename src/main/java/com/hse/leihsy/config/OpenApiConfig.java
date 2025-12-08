package com.hse.leihsy.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "LeihSy API",
                version = "1.0",
                description = "API documentation for the LeihSy backend"
                /*contact = @Contact(
                        name = "Max Mustermann",
                        email = "max@example.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )*/
        )
)
public class OpenApiConfig {
}