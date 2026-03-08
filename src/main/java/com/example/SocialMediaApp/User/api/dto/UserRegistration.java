package com.example.SocialMediaApp.User.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistration {
    @Size(min = 6,max = 10)
    @NotNull
    private String firstname;
    @Size(min = 6,max = 10)
    @NotNull
    private String lastname;
    @Size(min = 6,max = 10)
    @NotNull
    private String username;
    @Email
    private String email;
    @NotNull
    private String password;
    private LocalDate birthday;
}
