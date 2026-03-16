package com.example.userService.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

        private Map<String, Object> buildError(String error, String message, int status) {
                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", status);
                body.put("error", error);
                body.put("message", message);
                return body;
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
                return new ResponseEntity<>(
                                buildError("User not found",
                                                "The user you are looking for could not be found in the system.",
                                                HttpStatus.NOT_FOUND.value()),
                                HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(EmailAllreadyExistsException.class)
        public ResponseEntity<Map<String, Object>> handleEmailAllreadyExists(EmailAllreadyExistsException ex) {
                return new ResponseEntity<>(
                                buildError("Email already registered",
                                                "A user with this email address is already registered in the system.",
                                                HttpStatus.CONFLICT.value()),
                                HttpStatus.CONFLICT);
        }

        @ExceptionHandler(PersonalNumberAlreadyExistsException.class)
        public ResponseEntity<Map<String, Object>> handlePersonalNumberAlreadyExists(
                        PersonalNumberAlreadyExistsException ex) {
                return new ResponseEntity<>(
                                buildError("Personal number already registered",
                                                "A user with this personal number is already registered in the system.",
                                                HttpStatus.CONFLICT.value()),
                                HttpStatus.CONFLICT);
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
                return new ResponseEntity<>(
                                buildError("Invalid token",
                                                "The link you are trying to use is invalid. Please make sure you are using the correct link.",
                                                HttpStatus.UNAUTHORIZED.value()),
                                HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(TokenExpiredException.class)
        public ResponseEntity<Map<String, Object>> handleTokenExpired(TokenExpiredException ex) {
                return new ResponseEntity<>(
                                buildError("Token expired", "The link has expired. Please request a new link.",
                                                HttpStatus.UNAUTHORIZED.value()),
                                HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
                return new ResponseEntity<>(
                                buildError("Access denied",
                                                ex.getMessage() != null ? ex.getMessage()
                                                                : "You do not have permission to access this resource.",
                                                HttpStatus.FORBIDDEN.value()),
                                HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
                List<String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getDefaultMessage())
                                .collect(Collectors.toList());

                Map<String, Object> body = buildError("Validation error",
                                "Please check your input and try again.",
                                HttpStatus.BAD_REQUEST.value());
                body.put("details", errors);

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
                List<String> errors = ex.getConstraintViolations()
                                .stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.toList());

                Map<String, Object> body = buildError("Validation error", "The provided data does not meet the requirements.",
                                HttpStatus.BAD_REQUEST.value());
                body.put("details", errors);

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
                String error = "Invalid value provided. Please check that you are entering the correct type of information.";
                return new ResponseEntity<>(
                                buildError("Invalid value", error, HttpStatus.BAD_REQUEST.value()),
                                HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
                return new ResponseEntity<>(
                                buildError("Invalid argument", "One of the provided values is not valid.",
                                                HttpStatus.BAD_REQUEST.value()),
                                HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
                if (ex instanceof org.springframework.security.access.AccessDeniedException) {
                        // Låt Spring Security ta hand om detta
                        throw (org.springframework.security.access.AccessDeniedException) ex;
                }

                return new ResponseEntity<>(
                                buildError("An error occurred",
                                                "Something went wrong. Please try again later or contact support if the problem persists.",
                                                HttpStatus.INTERNAL_SERVER_ERROR.value()),
                                HttpStatus.INTERNAL_SERVER_ERROR);
        }
}