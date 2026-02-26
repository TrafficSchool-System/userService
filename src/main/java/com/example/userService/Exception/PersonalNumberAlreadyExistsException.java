package com.example.userService.Exception;

public class PersonalNumberAlreadyExistsException extends RuntimeException {

    public PersonalNumberAlreadyExistsException(String message) {
        super(message);
    }

    public PersonalNumberAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
