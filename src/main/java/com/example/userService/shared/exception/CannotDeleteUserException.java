package com.example.userService.shared.exception;

public class CannotDeleteUserException extends RuntimeException  {

    public CannotDeleteUserException(String message){
        super(message); 
    }

    public CannotDeleteUserException (String message, Throwable cause) {
        super(message, cause); 
    }

}
