package com.meilong.messenger.controller;

import com.meilong.messenger.dto.ApiError;
import com.meilong.messenger.service.TargetService.DuplicateTargetException;
import com.meilong.messenger.service.TargetService.TargetNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(TargetNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(TargetNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateTargetException.class)
    public ResponseEntity<ApiError> handleConflict(DuplicateTargetException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), message));
    }
}
