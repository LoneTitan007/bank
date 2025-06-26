package com.common.bank.exception;

/**
 * Base exception class for all banking-related exceptions
 */
public abstract class BankingException extends RuntimeException {
    
    private final String errorCode;
    
    public BankingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BankingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
