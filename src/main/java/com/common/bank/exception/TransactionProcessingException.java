package com.common.bank.exception;

/**
 * Exception thrown when transaction processing fails
 */
public class TransactionProcessingException extends BankingException {

    public TransactionProcessingException(String message, Throwable cause) {
        super("TRANSACTION_PROCESSING_ERROR", message, cause);
    }
}
