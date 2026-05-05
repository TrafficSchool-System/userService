package com.example.userService.shared.exception;

public class EmailAllreadyExistsException extends RuntimeException {

    public EmailAllreadyExistsException(String message){
        super(message); 
    }

    public EmailAllreadyExistsException (String message, Throwable cause) {
        super(message, cause); 
    }

}
