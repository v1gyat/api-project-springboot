package com.example.apiproject.exception;

import com.example.apiproject.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handler for validation errors
     * Triggered when @Valid annotation fails on request body
     * Returns HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error occurred: {}", ex.getMessage());

        // Extract field errors and build error map
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<?> response = new ApiResponse<>(
                false,
                "Validation failed",
                null,
                errors,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handler for ResourceNotFoundException
     * Returns HTTP 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());

        ApiResponse<?> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handler for UnauthorizedException (custom)
     * Returns HTTP 401 Unauthorized
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("Unauthorized access attempt: {}", ex.getMessage());

        ApiResponse<?> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handler for BadCredentialsException (Spring Security)
     * Thrown when login credentials are invalid
     * Returns HTTP 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials attempt: {}", ex.getMessage());

        ApiResponse<?> response = new ApiResponse<>(
                false,
                "Invalid credentials",
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handler for DisabledException (Spring Security)
     * Thrown when a deactivated user (isActive=false) attempts to login
     * Returns HTTP 403 Forbidden with clear messaging
     */
    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<ApiResponse<?>> handleDisabledException(
            org.springframework.security.authentication.DisabledException ex) {
        log.warn("Login attempt by deactivated account: {}", ex.getMessage());

        ApiResponse<?> response = new ApiResponse<>(
                false,
                "Account is deactivated. Please contact an administrator.",
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handler for ForbiddenException (custom)
     * Returns HTTP 403 Forbidden
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<?>> handleForbiddenException(ForbiddenException ex) {
        log.error("Forbidden access attempt: {}", ex.getMessage());

        ApiResponse<?> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handler for AccessDeniedException (Spring Security)
     * Thrown when user lacks permissions (@PreAuthorize fails)
     * Returns HTTP 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());

        ApiResponse<?> response = new ApiResponse<>(
                false,
                "Access denied",
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handler for BadRequestException (custom)
     * Returns HTTP 400 Bad Request
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequestException(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());

        ApiResponse<?> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handler for IllegalArgumentException
     * Returns HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());

        ApiResponse<?> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handler for all other exceptions (catch-all)
     * Returns HTTP 500 Internal Server Error
     * Logs the exception for debugging while returning generic message to client
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        // Log the exception with full stack trace for debugging
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiResponse<?> response = new ApiResponse<>(
                false,
                "An unexpected error occurred",
                null,
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
