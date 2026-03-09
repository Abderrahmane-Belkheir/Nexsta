package com.example.SocialMediaApp.User.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticatedUserService {

    private final ClientPropeties clientPropeties;
    private final WebClient webClient;
    private final RedisTemplate<String,String> redisTemplate;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issUri;

    public String getCurrentUser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null||!(authentication.getPrincipal() instanceof Jwt)){
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        String userId=((Jwt) authentication.getPrincipal()).getSubject();
        if(userId==null){
            throw new AuthenticationCredentialsNotFoundException("something went wrong trying to authenticate you please try later");
        }
        return userId;
    }

    public String callBack(String state,String authCode){
         String redirectUri= ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/auth/callback")
                .toUriString();

        if(redisTemplate.hasKey(state)){
            log.info("inside callback");
            redisTemplate.delete(state);
            String accesstoken=webClient.post().uri(issUri+"/protocol/openid-connect/token").
                    contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("client_id", clientPropeties.getId())
                            .with("client_secret", clientPropeties.getPassword())
                            .with("code", authCode)
                            .with("redirect_uri", redirectUri)
                    )
                    .retrieve()
                    .bodyToMono(Map.class).map(map -> (String) map.get("access_token")).block();

           return "/swagger.html#token=" + accesstoken;
        }
        return redirect();
    }

    public String redirect(){
       String redirectUri= ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/auth/callback")
                .toUriString();

        String state= UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(state,state);
        redisTemplate.expire(state,5, TimeUnit.MINUTES);
        return UriComponentsBuilder
                .fromHttpUrl(issUri+"/protocol/openid-connect/auth")
                .queryParam("client_id", clientPropeties.getId())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .toUriString();
    }

}
