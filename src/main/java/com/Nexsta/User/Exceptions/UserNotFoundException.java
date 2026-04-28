package com.Nexsta.User.Exceptions;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) {
        super("User Not Found :"+id);
    }
}
