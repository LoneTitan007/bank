package com.common.bank.exception;

/**
 * Exception thrown for invalid transaction operations
 */
public class InvalidTransactionException extends BankingException {
    
    public InvalidTransactionException(String message) {
        super("INVALID_TRANSACTION", message);
    }
    
    public static InvalidTransactionException invalidAmountException(Double amount) {
        return new InvalidTransactionException("Transaction amount must be positive: " + amount);
    }

    public static InvalidTransactionException sameAccountException() {
        return new InvalidTransactionException("Source and destination accounts cannot be the same");
    }
}
