package com.knu.coment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securityScheme = "Bearer Token";

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securityScheme);

        return new OpenAPI()
                .info(new Info()
                        .title("Comentor")
                        .version("v1")
                        .description("Comentor API TEST"))
                .components(new Components()
                        .addSecuritySchemes(securityScheme,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .security(Collections.singletonList(securityRequirement));
    }
}
