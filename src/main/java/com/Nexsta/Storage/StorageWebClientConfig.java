package com.Nexsta.Storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StorageWebClientConfig {

    private final WebClient.Builder webClientBuilder;
    private final StorageProperties storageEnv;

    @Bean(name = "storageWebClient")
    public WebClient webClient(){
        return webClientBuilder.baseUrl(storageEnv.getUrl()).defaultHeaders(headers -> {
            headers.set("Authorization", "Bearer " + storageEnv.getApiKey());
            headers.set("apikey", storageEnv.getApiKey());
            headers.set("x-upsert", "true");
        }).build();
    }

}
