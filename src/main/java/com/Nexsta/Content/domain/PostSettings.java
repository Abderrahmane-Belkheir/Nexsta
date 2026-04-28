package com.Nexsta.Content.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostSettings {

    private boolean commentsDisabled;
    private boolean hideLikes;
    private boolean hideComments;

}
