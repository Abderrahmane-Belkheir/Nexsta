package com.example.SocialMediaApp.Configurations.Websocket;


import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

     private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("inside the auth channel interceptor");
        StompHeaderAccessor accessor= MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if(accessor==null){
            throw new BadRequestException();
        }
        boolean ConnectCommand= Objects.equals(accessor.getCommand(), StompCommand.CONNECT);
        if(ConnectCommand){
            try{
                String header=accessor.getFirstNativeHeader("Authorization");
                if(header==null){
                    throw new BadRequestException();
                }
                String token=header.substring(7);
                Jwt jwt= jwtDecoder.decode(token);
               String userId = jwt.getSubject();
                accessor.setUser(()-> userId);
                log.info(userId);
            }catch (Exception e){
                throw new BadRequestException("something went wrong");
            }
        }
        return message;
    }
}

