package com.Nexsta.Configurations.Security;

import com.Nexsta.Storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties({StorageProperties.class})
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
        .csrf(AbstractHttpConfigurer::disable).
                oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> httpSecurityOAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults())).
                authorizeHttpRequests(authorization -> authorization.requestMatchers("api/v1/auth/**",
                        "/UI.html",
                        "/swagger-ui/**",
                        "/ws/**",
                        "/v3/api-docs/**",
                        "/create-post.html",
                        "/create-story.html",
                        "/edit-post.html",
                        "/api/v1/upload/confirm").permitAll().anyRequest().authenticated());
        return http.build();
    }

}
