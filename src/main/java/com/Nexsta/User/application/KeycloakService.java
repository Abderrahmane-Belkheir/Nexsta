package com.Nexsta.User.application;


import com.Nexsta.User.Exceptions.UserProvisioningException;
import com.Nexsta.User.api.dto.UserRegistration;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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
        userRepresentation.setEmailVerified(false);
        userRepresentation.setRequiredActions(List.of("VERIFY_EMAIL"));
        // no need for first and last name in keycloak
        userRepresentation.setFirstName("empty");
        userRepresentation.setLastName("empty");
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(userregistration.getPassword());
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        userRepresentation.setEnabled(true);
        try (Response response = realmResource.users().create(userRepresentation)) {

            int status = response.getStatus();

            if (status != 201) {
                String error = response.readEntity(String.class);
                log.error("Failed to provision user in auth server: {}", error);
                throw new UserProvisioningException("registration failed!!");
            }
            String userId = CreatedResponseUtil.getCreatedId(response);

            realmResource.users().get(userId).sendVerifyEmail();

            return userId;
        }
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
