package com.common.bank.exception;

/**
 * Exception thrown when a transaction is not found
 */
public class TransactionNotFoundException extends BankingException {
    
    public TransactionNotFoundException(String transactionId) {
        super("TRANSACTION_NOT_FOUND", "Transaction with ID " + transactionId + " not found");
    }
}
