package com.Nexsta.Content.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Location {
    private String locationName;
    private Double latitude;
    private Double longitude;
}
