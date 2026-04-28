package com.Nexsta.Shared;

import com.Nexsta.Messaging.api.dto.SendMessageToChatDTO;
import com.Nexsta.Messaging.api.dto.SendMessageToUserDTO;
import com.Nexsta.User.Exceptions.UserNotFoundException;
import com.Nexsta.User.persistence.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class UserExistence {

    private final UserRepo userRepo;


    @Before("@annotation(CheckUserExistence)")
    public void checkUserExistence(JoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        String userId=getUserId(args[0]);
                log.info("checking user existence for "+userId);
                if (!userRepo.existsById(userId)) {
                    throw new UserNotFoundException(userId);
                }
    }

    private String getUserId(Object object){
        return switch (object.getClass().getSimpleName()){
            case "String" -> (String) object;
            case "sendMessageToUserDTO" -> ((SendMessageToUserDTO) object).getUserId();
            case "sendMessageToChatDTO" -> ((SendMessageToChatDTO) object).getChatId();
            default -> throw new IllegalStateException("Unexpected value: " + object.getClass().getSimpleName());
        };
    }
}
