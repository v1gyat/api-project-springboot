package com.example.apiproject.exception;

/**
 * Exception thrown for business rule violations and invalid input
 * Results in HTTP 400 Bad Request response
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
