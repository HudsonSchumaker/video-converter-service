package com.example.file_conversion_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("error", "VALIDATION_ERROR");
        response.put("message", "Validation failed");
        response.put("details", errors);
        response.put("timestamp", LocalDateTime.now());
        
        logger.warn("Validation error: {}", errors);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(
            MaxUploadSizeExceededException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "FILE_TOO_LARGE");
        response.put("message", "File size exceeds maximum allowed size");
        response.put("timestamp", LocalDateTime.now());
        
        logger.warn("File size exceeded: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "INVALID_ARGUMENT");
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        
        logger.warn("Invalid argument: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "INTERNAL_ERROR");
        response.put("message", "An unexpected error occurred");
        response.put("timestamp", LocalDateTime.now());
        
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
