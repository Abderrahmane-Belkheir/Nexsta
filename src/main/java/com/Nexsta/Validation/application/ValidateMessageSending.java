package com.Nexsta.Validation.application;

import com.Nexsta.Messaging.api.dto.SendMessage;
import com.Nexsta.Validation.Annotations.ValidMessage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidateMessageSending implements ConstraintValidator<ValidMessage, SendMessage> {
    @Override
    public boolean isValid(SendMessage message, ConstraintValidatorContext constraintValidatorContext) {
        if((message.getRecipientId()!=null&&message.getChatId()==null)||(message.getRecipientId()==null&&message.getChatId()!=null)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Provide either chat_id or recipient_id, not both or neither").addConstraintViolation();
            return false;
        }
        return true;
    }
}
