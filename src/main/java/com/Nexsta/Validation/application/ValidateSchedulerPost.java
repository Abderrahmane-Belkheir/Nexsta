package com.Nexsta.Validation.application;

import com.Nexsta.Content.api.dto.PostCreationRequest;
import com.Nexsta.Validation.Annotations.ValidScheduled;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

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
