package com.example.userService.shared.exception;

/**
 * Exception som kastas när en användare försöker komma åt resurser
 * som de inte har behörighet till.
 * 
 * Används för authorization-checks i service layer.
 * GlobalExceptionHandler hanterar denna och returnerar 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
