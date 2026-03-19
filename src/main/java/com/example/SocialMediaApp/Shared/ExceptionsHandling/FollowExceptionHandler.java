package com.example.SocialMediaApp.Shared.ExceptionsHandling;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.User.Exceptions.UserNotFoundException;
import jdk.jfr.Experimental;
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
}
