package com.common.bank.exception;

/**
 * Exception thrown for invalid transaction operations
 */
public class InvalidTransactionException extends BankingException {
    
    public InvalidTransactionException(String message) {
        super("INVALID_TRANSACTION", message);
    }
    
    public static InvalidTransactionException nonPositiveAmountException(Double amount) {
        return new InvalidTransactionException("Transaction amount must be positive: " + amount);
    }

    public static InvalidTransactionException nullAmountException() {
        return new InvalidTransactionException("Transaction amount cannot be null: ");
    }

    public static InvalidTransactionException sameAccountException() {
        return new InvalidTransactionException("Source and destination accounts cannot be the same");
    }
}
