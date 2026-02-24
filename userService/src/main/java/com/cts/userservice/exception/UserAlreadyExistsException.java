package com.cts.userservice.exception;

public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String email, String field) {
        super(field + " already exists: " + email);
    }
}
