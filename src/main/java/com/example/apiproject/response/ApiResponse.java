package com.example.apiproject.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object errors;
    private LocalDateTime timestamp;

    /**
     * Constructor for successful responses without errors
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = null;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Static factory method for successful responses
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    /**
     * Static factory method for error responses
     */
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return new ApiResponse<>(false, message, null, errors, LocalDateTime.now());
    }
}
