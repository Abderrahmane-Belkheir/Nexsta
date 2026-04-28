package com.Nexsta.Profile.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileSettingsDto {
    private boolean isPrivate;
    private boolean showActivity;
}
