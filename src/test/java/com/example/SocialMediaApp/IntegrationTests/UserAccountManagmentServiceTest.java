package com.example.SocialMediaApp.IntegrationTests;

import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Notification.persistence.NotificationSettingsRepo;
import com.example.SocialMediaApp.Profile.persistence.ProfileRepo;
import com.example.SocialMediaApp.User.api.dto.UserRegistration;
import com.example.SocialMediaApp.User.application.RegistrationService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class UserAccountManagmentServiceTest extends TestContainerConfig{

    private final RegistrationService registrationService;
    private final UserRepo userRepo;
    private final ProfileRepo profileRepo;
    private final NotificationSettingsRepo notificationSettingsRepo;
    private final JwtDecoder jwtDecoder;

    private String createuser(){
        String username= UUID.randomUUID().toString().substring(0,7);
        UserRegistration userregistration=
                new UserRegistration("test","test",username,username+"@gmail.com","test", LocalDate.now());
        return  registrationService.registerUser(userregistration);
    }

    @Test
    public void duplicateUser_throwsRuntimeException(){
        String userId=createuser();
        User user=userRepo.findById(userId).get();
        UserRegistration userregistration=
                new UserRegistration("test","test",user.getUsername()
                        ,"test@gmail.com","test", LocalDate.now());
        assertThrows(RuntimeException.class, ()->registrationService.registerUser(userregistration));
    }

    @Test
    public void registeruser() throws IOException {
        String userId=createuser();
        Optional<User>  user=userRepo.findById(userId);
        assertTrue(user.isPresent());
        log.info("user id :"+userId);
        assertTrue(profileRepo.existsByUserId(userId));
        NotificationsSettings notificationsSettings=notificationSettingsRepo.findByUserId(userId);
        assertTrue(notificationsSettings.getOnfollow());
        assertTrue(notificationsSettings.getOnfollowingrequestAccepted());
        assertTrue(notificationsSettings.getOnfollowingrequestRejected());
        //checking if saved in keycloak
        Keycloak keycloakTest=KeycloakBuilder.builder().
                realm("master").username("admin").password("Admin123!").
                serverUrl("http://localhost:8080").clientId("admin-cli").build();
        assertEquals(1,keycloakTest.realm("Realm").users().searchByUsername(user.get().getUsername(),true).size());

       // testing jwt decoder with the registred user
        WebClient webClient=WebClient.builder().
                baseUrl("http://localhost:8080/realms/Realm/protocol/openid-connect/token").
                defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE).build();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "SocialMediaApp");
        form.add("client_secret","gOV165Fr6I1YODaJv7MlTMtOxnuvfpoq");
        form.add("username", user.get().getUsername());
        form.add("password", "test");
        String response=webClient.post().body(BodyInserters.fromFormData(form)).retrieve().bodyToMono(String.class).block();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        String accessToken = node.get("access_token").asText();
        assertDoesNotThrow(()->jwtDecoder.decode(accessToken));
        assertEquals(jwtDecoder.decode(accessToken).getSubject(),userId);
    }

}
