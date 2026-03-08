package com.example.SocialMediaApp.User.application;
import com.example.SocialMediaApp.User.api.dto.UserRegistration;


public interface IdentityService {
    String UserProvision(UserRegistration userregistration);
    void UserRemoval(String userId);
    void changeUsername(String userId,String username);
}
