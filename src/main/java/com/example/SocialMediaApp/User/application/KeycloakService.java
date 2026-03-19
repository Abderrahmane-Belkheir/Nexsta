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

    private final RealmResource realmResource;

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
        Response response= realmResource.users().create(userRepresentation);
            if(response.getStatus()!=201){
                response.close();
                log.error("failed to provision user in auth server "+response.readEntity(String.class));
                throw new UserProvisioningException("registration failed!!");
            }
       return  CreatedResponseUtil.getCreatedId(response);
    }

    public void UserRemoval(String userId){
        realmResource.users().get(userId).remove();
    }

    public void changeUsername(String userId,String username){
        try{
            UserResource userResource= realmResource.users().get(userId);
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
