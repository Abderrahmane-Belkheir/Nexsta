package com.example.SocialMediaApp.Notification.Configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class EmailSendingWebClient {

    private final EmailSendingProperties emailSendingProperties;
    private final WebClient.Builder webClientBuilder;

    @Bean(name = "emailWebClient")
    public WebClient webClient(){
        return webClientBuilder.baseUrl(emailSendingProperties.getUrl()).defaultHeaders(headers -> {
            headers.set("api-key", emailSendingProperties.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);
        }).build();
    }
}
