package com.example.SocialMediaApp.Profile.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileSettings {
    private boolean isPrivate;
    private boolean showAcitivity;
}
