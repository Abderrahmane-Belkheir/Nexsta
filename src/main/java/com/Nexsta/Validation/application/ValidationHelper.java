package com.Nexsta.Validation.application;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ValidationHelper {

    public static boolean validateInstant(Instant date,ConstraintValidatorContext constraintValidatorContext){
        Instant now=Instant.now();
        Instant min=now.plus(2, ChronoUnit.HOURS);
        Instant max=now.plus(90,ChronoUnit.DAYS);
        constraintValidatorContext.disableDefaultConstraintViolation();
        if(date.isBefore(min)){
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("Post must be Scheduled at least 2 hours in advance").
                    addConstraintViolation();
            return false;
        }

        if(date.isAfter(max)){
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("Post cannot be Scheduled more than 90 days out").
                    addConstraintViolation();
            return false;
        }
        return true;
    }

}
