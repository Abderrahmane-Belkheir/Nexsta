package com.Nexsta.User.api.Controllers;

import com.Nexsta.User.api.dto.UserRegistration;
import com.Nexsta.User.application.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
