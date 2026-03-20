package com.example.SocialMediaApp.User.application;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RealmResource {

    @Value("${keycloak.username}")
    private String username;
    @Value("${keycloak.password}")
    private String password;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public org.keycloak.admin.client.resource.RealmResource getRealmResource(){
        Keycloak keycloak= KeycloakBuilder.builder().
                realm("master").username(username).password(password).
                serverUrl(issuerUri.substring(0, issuerUri.indexOf("/realms"))).clientId("admin-cli").build();
        String realmName=issuerUri.substring(issuerUri.lastIndexOf("/")+1);
        return keycloak.realm(realmName);
    }

}
