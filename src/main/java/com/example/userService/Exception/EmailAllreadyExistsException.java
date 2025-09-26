package com.example.userService.Exception;

public class EmailAllreadyExistsException extends RuntimeException {

    public EmailAllreadyExistsException(String message){
        super(message); 
    }

    public EmailAllreadyExistsException (String message, Throwable cause) {
        super(message, cause); 
    }

}
