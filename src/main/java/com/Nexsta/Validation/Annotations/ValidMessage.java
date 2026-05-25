package com.Nexsta.Validation.Annotations;

import com.Nexsta.Validation.application.ValidateMessageSending;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateMessageSending.class)
public @interface ValidMessage {
    String message() default "Invalid Format";
    Class<?>[] groups() default {};
    Class<? extends Payload> [] payload() default {};
}
