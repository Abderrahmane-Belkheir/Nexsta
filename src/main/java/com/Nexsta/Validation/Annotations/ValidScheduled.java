package com.Nexsta.Validation.Annotations;

import com.Nexsta.Validation.application.ValidateScheduledInstant;
import com.Nexsta.Validation.application.ValidateSchedulerPost;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy ={ValidateSchedulerPost.class, ValidateScheduledInstant.class})
public @interface ValidScheduled {
    String message() default "Invalid Format";
    Class<?>[] groups() default {};
    Class<? extends Payload> [] payload() default {};
}
