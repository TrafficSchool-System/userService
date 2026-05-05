package com.example.userService.shared.exception;

public class PersonalNumberAlreadyExistsException extends RuntimeException {

    public PersonalNumberAlreadyExistsException(String message) {
        super(message);
    }

    public PersonalNumberAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
