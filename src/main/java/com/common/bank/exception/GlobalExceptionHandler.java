package com.common.bank.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
        private String errorCode;
    }
    
    // ========== Banking-Specific Exception Handlers ==========
    
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException e) {
        log.warn("Account not found: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("NOT_FOUND", e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAccountAlreadyExistsException(AccountAlreadyExistsException e) {
        log.warn("Account already exists: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("CONFLICT", e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(InvalidBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBalanceException(InvalidBalanceException e) {
        log.warn("Invalid balance: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", e.getMessage(), e.getErrorCode());
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFoundException(TransactionNotFoundException e) {
        log.warn("Transaction not found: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("NOT_FOUND", e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        String message = "Required field is missing or null";
        log.warn("Null pointer exception for URI {}: {}", request.getRequestURI(), e.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", message, "MISSING_REQUIRED_FIELD");
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException e) {
        log.warn("Insufficient balance: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", e.getMessage(), e.getErrorCode());
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransactionException(InvalidTransactionException e) {
        log.warn("Invalid transaction: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", e.getMessage(), e.getErrorCode());
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(TransactionProcessingException.class)
    public ResponseEntity<ErrorResponse> handleTransactionProcessingException(TransactionProcessingException e) {
        log.error("Transaction processing error: {}", e.getMessage(), e);
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(BankingException.class)
    public ResponseEntity<ErrorResponse> handleBankingException(BankingException e) {
        log.error("Banking system error: {}", e.getMessage(), e);
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", e.getMessage(), e.getErrorCode());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    // ========== Framework-Level Exception Handlers ==========
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error for URI {}: {}", request.getRequestURI(), errorMessage);
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", errorMessage, "VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        String message = "Invalid JSON format or data type mismatch";
        if (e.getMessage() != null && e.getMessage().contains("Cannot deserialize")) {
            message = "Invalid data format in request body";
        }
        log.warn("JSON deserialization error for URI {}: {}", request.getRequestURI(), e.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", message, "INVALID_JSON");
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s", 
                e.getMethod(), String.join(", ", e.getSupportedMethods()));
        log.warn("Method not allowed for URI {}: {}", request.getRequestURI(), message);
        ErrorResponse error = new ErrorResponse("METHOD_NOT_ALLOWED", message, "METHOD_NOT_ALLOWED");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }
    
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        String message = String.format("Content type '%s' is not supported. Supported types: %s", 
                e.getContentType(), e.getSupportedMediaTypes().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")));
        log.warn("Unsupported media type for URI {}: {}", request.getRequestURI(), message);
        ErrorResponse error = new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", message, "UNSUPPORTED_MEDIA_TYPE");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                e.getValue(), e.getName(), e.getRequiredType().getSimpleName());
        log.warn("Type mismatch error for URI {}: {}", request.getRequestURI(), message);
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", message, "TYPE_MISMATCH");
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        String message = String.format("No endpoint found for %s %s", e.getHttpMethod(), e.getResourcePath());
        log.warn("No resource found: {}", message);
        ErrorResponse error = new ErrorResponse("NOT_FOUND", message, "ENDPOINT_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // ========== Generic Exception Handler ==========
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {
        // Don't handle SpringDoc/OpenAPI related requests
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/v3/api-docs") || requestURI.contains("/swagger-ui")) {
            log.error("SpringDoc error for URI {}: {}", requestURI, e.getMessage(), e);
            throw new RuntimeException(e); // Let SpringDoc handle it
        }
        
        log.error("Unexpected error for URI {}: {}", requestURI, e.getMessage(), e);
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred", "SYSTEM_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
