package com.example.SocialMediaApp.Configurations.Swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUrl;

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("keycloak"))
                .components(new Components()
                        .addSecuritySchemes("keycloak",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .authorizationCode(
                                                        new OAuthFlow()
                                                                .authorizationUrl(authUrl())
                                                                .tokenUrl(tokenUrl()).scopes(new Scopes()
                                                                        .addString("openid", "openid")
                                                                        .addString("profile", "profile")
                                                                        .addString("email", "email")
                                                                )
                                                )
                                        )
                        )
                );
    }

    private String authUrl() {
        return issuerUrl+"/protocol/openid-connect/auth";
    }

    private String tokenUrl() {
        return issuerUrl+"/protocol/openid-connect/token";
    }
}
