package com.example.SocialMediaApp.Validation.application;

import com.example.SocialMediaApp.Content.api.dto.PostCreationRequest;
import com.example.SocialMediaApp.Validation.Annotations.ValidScheduled;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
@Slf4j
public class ValidateSchedulerPost implements ConstraintValidator<ValidScheduled, PostCreationRequest> {


    @Override
    public boolean isValid(PostCreationRequest request, ConstraintValidatorContext constraintValidatorContext) {
        if(request.getPostAction()== PostCreationRequest.PostAction.SCHEDULED){
            Instant date=request.getScheduleAt();
            return date!=null&&ValidationHelper.validateInstant(request.getScheduleAt(),constraintValidatorContext);
        }
        return true;
    }


}
