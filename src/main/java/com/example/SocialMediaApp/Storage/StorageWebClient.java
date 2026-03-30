package com.example.SocialMediaApp.Storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class StorageWebClient {

    private final WebClient.Builder webClientBuilder;
    private final StorageProperties storageEnv;

    @Bean
    public WebClient webClient(){
        return webClientBuilder.baseUrl(storageEnv.getUrl()).defaultHeaders(headers -> {
            headers.set("Authorization", "Bearer " + storageEnv.getApiKey());
            headers.set("apikey", storageEnv.getApiKey());
            headers.set("x-upsert", "true");
        }).build();
    }
}
