package com.Nexsta.Upload.Exceptions;

import lombok.Getter;

import java.util.List;
@Getter
public class UnsupportedMediaTypeException extends RuntimeException {
    private final List<String> supportedMediaType;
    public UnsupportedMediaTypeException(List<String> supportedMediaType){
        this.supportedMediaType=supportedMediaType;
    }
}
