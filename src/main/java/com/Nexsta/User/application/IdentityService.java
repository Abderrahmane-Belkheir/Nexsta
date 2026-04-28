package com.Nexsta.User.application;
import com.Nexsta.User.api.dto.UserRegistration;


public interface IdentityService {
    String UserProvision(UserRegistration userregistration);
    void UserRemoval(String userId);
    void changeUsername(String userId,String username);
}
