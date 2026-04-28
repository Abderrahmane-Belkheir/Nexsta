package com.Nexsta.Shared.ExceptionsHandling;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Content.Exceptions.ContentNotFoundException;
import com.Nexsta.Shared.Exceptions.ActionNotAllowedException;
import com.Nexsta.User.Exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class FollowExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotExist(UserNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message:",e.getMessage()));
    }
    @ExceptionHandler(ContentNotAvailableException.class)
    public ResponseEntity<Map<String,String>> handleContentNotAvailable(ContentNotAvailableException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message:",e.getMessage()));
    }
    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleContentNotFound(ContentNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message:",e.getMessage()));
    }
    @ExceptionHandler(ActionNotAllowedException.class)
    public ResponseEntity<Map<String,String>> handleActionNotAllowed(ActionNotAllowedException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message",e.getMessage()));
    }
}
