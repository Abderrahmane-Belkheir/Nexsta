package com.example.SocialMediaApp.User.application;


import com.example.SocialMediaApp.User.Exceptions.UserProvisioningException;
import com.example.SocialMediaApp.User.api.dto.UserRegistration;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService implements IdentityService {

    // injecting keycloak details from properties
    @Value("${keycloak.username}")
    private String username;
    @Value("${keycloak.password}")
    private String password;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private RealmResource getRealmResource(){
        Keycloak keycloak= KeycloakBuilder.builder().
                realm("master").username(username).password(password).
                serverUrl(issuerUri.substring(0, issuerUri.indexOf("/realms"))).clientId("admin-cli").build();
        String realmName=issuerUri.substring(issuerUri.lastIndexOf("/")+1);
      return keycloak.realm(realmName);

    }

    public void UserRemoval(String userId){
        getRealmResource().users().get(userId).remove();
    }

    // method responsible for creating the user record inside the identity provider


    public String  UserProvision(UserRegistration userregistration){
        org.keycloak.representations.idm.UserRepresentation userRepresentation= new org.keycloak.representations.idm.UserRepresentation();
        userRepresentation.setEmail(userregistration.getEmail());
        userRepresentation.setUsername(userregistration.getUsername());
        // no need for first and last name in keycloak
        userRepresentation.setFirstName("empty");
        userRepresentation.setLastName("empty");
        userRepresentation.setEmailVerified(true);
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(userregistration.getPassword());
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        userRepresentation.setEnabled(true);
        Response response= getRealmResource().users().create(userRepresentation);
            if(response.getStatus()!=201){
                response.close();
                log.error("failed to provision user in auth server "+response.readEntity(String.class));
                throw new UserProvisioningException("registration failed!!");
            }
       return  CreatedResponseUtil.getCreatedId(response);
    }

    public void changeUsername(String userId,String username){
        try{
            UserResource userResource= getRealmResource().users().get(userId);
            UserRepresentation userRepresentation=userResource.toRepresentation();
            userRepresentation.setUsername(username);
            userResource.update(userRepresentation);
            if(!username.equals(userResource.toRepresentation().getUsername())){
                throw new RuntimeException("failed to change username!!");
            }
        }catch (ClientErrorException e){
           throw new RuntimeException("username already exists!!");
        }
    }
}
