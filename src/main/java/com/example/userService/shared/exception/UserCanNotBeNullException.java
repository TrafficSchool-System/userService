package com.example.userService.shared.exception;

public class UserCanNotBeNullException extends RuntimeException {

    //Konstruktor
    public UserCanNotBeNullException (String message) {
        super(message);
    
    }

    public UserCanNotBeNullException(String message, Throwable cause){
        super(message, cause); 
    }

}
