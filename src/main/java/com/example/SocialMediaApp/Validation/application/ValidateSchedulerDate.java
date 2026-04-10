package com.example.SocialMediaApp.Validation.application;

import com.example.SocialMediaApp.Content.api.dto.PostCreationRequest;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Validation.Annotations.ValidScheduled;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
@Slf4j
public class ValidateSchedulerDate implements ConstraintValidator<ValidScheduled, PostCreationRequest> {

    @Override
    public boolean isValid(PostCreationRequest request, ConstraintValidatorContext constraintValidatorContext) {
        if(request.getPostAction()== PostCreationRequest.PostAction.SCHEDULED){
            Instant date=request.getScheduleAt();
            if(date==null) return false;
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
        }
        return true;
    }


}
