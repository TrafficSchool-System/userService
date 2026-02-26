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
                                buildError("Användare hittades inte",
                                                "Den användare du söker efter kunde inte hittas i systemet.",
                                                HttpStatus.NOT_FOUND.value()),
                                HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(EmailAllreadyExistsException.class)
        public ResponseEntity<Map<String, Object>> handleEmailAllreadyExists(EmailAllreadyExistsException ex) {
                return new ResponseEntity<>(
                                buildError("E-post redan registrerad",
                                                "En användare med denna e-postadress finns redan registrerad i systemet.",
                                                HttpStatus.CONFLICT.value()),
                                HttpStatus.CONFLICT);
        }

        @ExceptionHandler(PersonalNumberAlreadyExistsException.class)
        public ResponseEntity<Map<String, Object>> handlePersonalNumberAlreadyExists(
                        PersonalNumberAlreadyExistsException ex) {
                return new ResponseEntity<>(
                                buildError("Personnummer redan registrerat",
                                                "En användare med detta personnummer finns redan registrerad i systemet.",
                                                HttpStatus.CONFLICT.value()),
                                HttpStatus.CONFLICT);
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
                return new ResponseEntity<>(
                                buildError("Ogiltig token",
                                                "Länken du försöker använda är ogiltig. Kontrollera att du använder rätt länk.",
                                                HttpStatus.UNAUTHORIZED.value()),
                                HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(TokenExpiredException.class)
        public ResponseEntity<Map<String, Object>> handleTokenExpired(TokenExpiredException ex) {
                return new ResponseEntity<>(
                                buildError("Token har gått ut", "Länken har gått ut. Vänligen begär en ny länk.",
                                                HttpStatus.UNAUTHORIZED.value()),
                                HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
                return new ResponseEntity<>(
                                buildError("Åtkomst nekad",
                                                ex.getMessage() != null ? ex.getMessage()
                                                                : "Du har inte behörighet att komma åt denna resurs.",
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

                Map<String, Object> body = buildError("Valideringsfel",
                                "Vänligen kontrollera dina uppgifter och försök igen.",
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

                Map<String, Object> body = buildError("Valideringsfel", "Uppgifterna uppfyller inte kraven.",
                                HttpStatus.BAD_REQUEST.value());
                body.put("details", errors);

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
                String error = "Ogiltigt värde angett. Vänligen kontrollera att du skriver in rätt typ av information.";
                return new ResponseEntity<>(
                                buildError("Ogiltigt värde", error, HttpStatus.BAD_REQUEST.value()),
                                HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
                return new ResponseEntity<>(
                                buildError("Ogiltigt argument", "Något av de angivna värdena är inte giltigt.",
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
                                buildError("Ett fel uppstod",
                                                "Något gick fel. Vänligen försök igen senare eller kontakta support om problemet kvarstår.",
                                                HttpStatus.INTERNAL_SERVER_ERROR.value()),
                                HttpStatus.INTERNAL_SERVER_ERROR);
        }
}