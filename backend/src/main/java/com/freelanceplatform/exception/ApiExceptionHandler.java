package com.freelanceplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage(), (a, b) -> a));
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "status", 400, "error", "Validation failed", "fields", errors));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "status", 400, "error", exception.getMessage()));
    }
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("timestamp", Instant.now(), "status", 401, "error", "Invalid email or password"));
    }
}
