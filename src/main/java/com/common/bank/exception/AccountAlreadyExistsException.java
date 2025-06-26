package com.common.bank.exception;

/**
 * Exception thrown when trying to create an account that already exists
 */
public class AccountAlreadyExistsException extends BankingException {
    
    public AccountAlreadyExistsException(String accountId) {
        super("ACCOUNT_ALREADY_EXISTS", "Account with ID " + accountId + " already exists");
    }
}
