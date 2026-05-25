package com.Nexsta.Shared.ExceptionsHandling;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class DtoValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleNotValidDto(MethodArgumentNotValidException e){
        Map<String,String> map=new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error ->{
            if (error instanceof FieldError fieldError) {
                map.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                map.put(error.getObjectName(), error.getDefaultMessage());
            }
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
    }

}
