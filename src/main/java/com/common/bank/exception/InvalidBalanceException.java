package com.common.bank.exception;

/**
 * Exception thrown when an invalid balance is provided
 */
public class InvalidBalanceException extends BankingException {
    
    public InvalidBalanceException(String message) {
        super("INVALID_BALANCE", message);
    }
    
    public static InvalidBalanceException nonPositiveBalanceException(double balance) {
        return new InvalidBalanceException("Initial balance cannot be non positive: " + balance);
    }
}
