package com.common.bank.exception;

/**
 * Exception thrown when an account is not found
 */
public class AccountNotFoundException extends BankingException {
    
    public AccountNotFoundException(String accountId) {
        super("ACCOUNT_NOT_FOUND", "Account with ID " + accountId + " not found");
    }
}
