package com.example.apiproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenReusedException extends RuntimeException {
    public TokenReusedException(String message) {
        super(message);
    }
}
