package com.example.tdg.exception;

/**
 * Exception thrown when a template is not found.
 */
public class TemplateNotFoundException extends RuntimeException {
    
    public TemplateNotFoundException(String message) {
        super(message);
    }
    
    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when a schedule is not found.
 */
class ScheduleNotFoundException extends RuntimeException {
    
    public ScheduleNotFoundException(String message) {
        super(message);
    }
    
    public ScheduleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when data generation fails.
 */
class DataGenerationException extends RuntimeException {
    
    public DataGenerationException(String message) {
        super(message);
    }
    
    public DataGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when PDF analysis fails.
 */
class PDFAnalysisException extends RuntimeException {
    
    public PDFAnalysisException(String message) {
        super(message);
    }
    
    public PDFAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when a validation error occurs.
 */
class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
