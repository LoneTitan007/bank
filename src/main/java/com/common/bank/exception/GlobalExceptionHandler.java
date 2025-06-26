package com.common.bank.exception;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
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
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", e.getMessage(), "INVALID_ARGUMENT");
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error for URI {}: {}", request.getRequestURI(), errorMessage);
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", errorMessage, "VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(error);
    }
    
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
