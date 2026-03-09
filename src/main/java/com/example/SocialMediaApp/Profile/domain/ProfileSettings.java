package com.example.SocialMediaApp.Profile.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ProfileSettings {
    private boolean isPrivate;
    private boolean showActivity;
}