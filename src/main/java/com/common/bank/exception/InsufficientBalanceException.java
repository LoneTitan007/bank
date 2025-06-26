package com.common.bank.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when there's insufficient balance for a transaction
 */
public class InsufficientBalanceException extends BankingException {
    
    public InsufficientBalanceException(Double available, Double required) {
        super("INSUFFICIENT_BALANCE", 
              "Insufficient balance in source account. Available: " + available + ", Required: " + required);
    }
}
