package com.postkar.project3dmodel.exception;

import com.postkar.project3dmodel.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR", 
            "An unexpected error occurred"
        );
        error.setPath(request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Validation failed");
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR", 
            message
        );
        error.setPath(request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "CONSTRAINT_VIOLATION", 
            ex.getMessage()
        );
        error.setPath(request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MarkerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMarkerNotFound(MarkerNotFoundException ex, WebRequest request) {
        logger.warn("Marker not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "MARKER_NOT_FOUND", 
            ex.getMessage()
        );
        error.setPath(request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DatabaseBuildException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseBuildException(DatabaseBuildException ex, WebRequest request) {
        logger.error("Database build failed: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "DATABASE_BUILD_FAILED", 
            ex.getMessage()
        );
        error.setPath(request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime exception occurred: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "RUNTIME_ERROR", 
            ex.getMessage()
        );
        error.setPath(request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
