package com.example.SocialMediaApp.User.api.Controllers;

import com.example.SocialMediaApp.User.api.dto.UserRegistration;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.application.RegistrationService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public void register(@RequestBody @Valid UserRegistration user) {
        registrationService.registerUser(user);
    }

}
