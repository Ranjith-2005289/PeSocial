package com.pesocial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI peSocialOpenApi() {
        final String bearerScheme = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("PESocial API")
                .description("Backend APIs for PESocial (visibility, ownership, paywall, messaging)")
                .version("v1"))
            .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
            .components(new Components()
                .addSecuritySchemes(bearerScheme, new SecurityScheme()
                    .name(bearerScheme)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
