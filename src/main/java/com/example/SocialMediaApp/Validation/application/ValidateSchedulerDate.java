package com.example.SocialMediaApp.Validation.application;

import com.example.SocialMediaApp.Validation.Annotations.ValidScheduled;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
@Slf4j
public class ValidateSchedulerDate implements ConstraintValidator<ValidScheduled, Instant> {

    @Override
    public boolean isValid(Instant instant, ConstraintValidatorContext constraintValidatorContext) {
        if(instant==null) return true;
        Instant now=Instant.now();
        Instant min=now.plus(2, ChronoUnit.HOURS);
        Instant max=now.plus(90,ChronoUnit.DAYS);
        constraintValidatorContext.disableDefaultConstraintViolation();
        if(instant.isBefore(min)){
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("Post must be Scheduled at least 2 hours in advance").
                    addConstraintViolation();
            return false;
        }

        if(instant.isAfter(max)){
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("Post cannot be Scheduled more than 90 days out").
                    addConstraintViolation();
            return false;
        }
        return true;
    }

}
