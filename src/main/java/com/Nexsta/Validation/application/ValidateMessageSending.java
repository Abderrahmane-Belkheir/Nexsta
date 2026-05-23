package com.Nexsta.Validation.application;

import com.Nexsta.Messaging.api.dto.SendMessageDTO;
import com.Nexsta.Validation.Annotations.ValidMessage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidateMessageSending implements ConstraintValidator<ValidMessage, SendMessageDTO> {
    @Override
    public boolean isValid(SendMessageDTO message, ConstraintValidatorContext constraintValidatorContext) {
        if((message.getRecipientId()!=null&&message.getChatId()==null)||(message.getRecipientId()==null&&message.getChatId()!=null)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Provide either chat_id or recipient_id, not both or neither").addConstraintViolation();
            return false;
        }
        return true;
    }
}
