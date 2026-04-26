package com.example.SocialMediaApp.User.application;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RealmResource {

    @Value("${keycloak.clientId}")
    private String clientId;
    @Value("${keycloak.clientSecret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public org.keycloak.admin.client.resource.RealmResource getRealmResource(){
        String realmName=issuerUri.substring(issuerUri.lastIndexOf("/")+1);

        log.info("realmName:{}", realmName);

        Keycloak keycloak= KeycloakBuilder.builder().
                serverUrl(issuerUri.substring(0, issuerUri.indexOf("/realms")))
                .realm(realmName).clientId(clientId).clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS).build();

        return keycloak.realm(realmName);
    }

}
