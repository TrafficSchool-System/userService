package com.example.userService.shared.exception;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException (String message){
        super(message); 
    }

    public TokenExpiredException (String message, Throwable cause){
        super(message, cause); 
    }

    



}
