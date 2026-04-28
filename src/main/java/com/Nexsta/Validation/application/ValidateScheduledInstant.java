package com.Nexsta.Validation.application;

import com.Nexsta.Validation.Annotations.ValidScheduled;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Instant;

public class ValidateScheduledInstant implements ConstraintValidator<ValidScheduled, Instant> {

    @Override
    public boolean isValid(Instant instant, ConstraintValidatorContext constraintValidatorContext) {
        return instant==null||ValidationHelper.validateInstant(instant,constraintValidatorContext);
    }

}
