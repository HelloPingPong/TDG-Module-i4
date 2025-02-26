package com.example.tdg.exception;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle TemplateNotFoundException.
     */
    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<Object> handleTemplateNotFoundException(
            TemplateNotFoundException ex, WebRequest request) {
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.NOT_FOUND, ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle ScheduleNotFoundException.
     */
    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<Object> handleScheduleNotFoundException(
            ScheduleNotFoundException ex, WebRequest request) {
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.NOT_FOUND, ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle DataGenerationException.
     */
    @ExceptionHandler(DataGenerationException.class)
    public ResponseEntity<Object> handleDataGenerationException(
            DataGenerationException ex, WebRequest request) {
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle PDFAnalysisException.
     */
    @ExceptionHandler(PDFAnalysisException.class)
    public ResponseEntity<Object> handlePDFAnalysisException(
            PDFAnalysisException ex, WebRequest request) {
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle ValidationException.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle ConstraintViolationException.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.BAD_REQUEST, "Validation error: " + message, request);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle SchedulerException.
     */
    @ExceptionHandler(SchedulerException.class)
    public ResponseEntity<Object> handleSchedulerException(
            SchedulerException ex, WebRequest request) {
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Scheduler error: " + ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle MaxUploadSizeExceededException.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.PAYLOAD_TOO_LARGE, "File upload too large: " + ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    }
    
    /**
     * Handle IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle general Exception.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        
        logger.error("Uncaught exception", ex);
        
        Map<String, Object> body = createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage(), request);
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle validation errors from @Valid annotations.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> body = createErrorBody(status, "Validation error", request);
        body.put("errors", errors);
        
        return new ResponseEntity<>(body, headers, status);
    }
    
    /**
     * Create a standardized error response body.
     */
    private Map<String, Object> createErrorBody(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return body;
    }
}
