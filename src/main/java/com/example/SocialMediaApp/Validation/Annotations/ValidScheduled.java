package com.example.SocialMediaApp.Validation.Annotations;

import com.example.SocialMediaApp.Validation.application.ValidateSchedulerDate;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateSchedulerDate.class)
public @interface ValidScheduled {
    String message() default "Invalid Format";
    Class<?>[] groups() default {};
    Class<? extends Payload> [] payload() default {};
}
