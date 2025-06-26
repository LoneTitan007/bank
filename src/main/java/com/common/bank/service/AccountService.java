package com.common.bank.service;

import com.common.bank.dto.AccountCreateRequest;
import com.common.bank.dto.AccountCreationResponse;
import com.common.bank.dto.AccountResponse;
import com.common.bank.exception.AccountAlreadyExistsException;
import com.common.bank.exception.AccountNotFoundException;
import com.common.bank.model.Account;
import com.common.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountCreationResponse createAccount(AccountCreateRequest request) {
        log.info("Creating account with ID: {}", request.getAccountId());

        // Check if account already exists
        if (accountRepository.findByRefId(request.getAccountId()).isPresent()) {
            log.warn("Account creation failed - Account with ID {} already exists", request.getAccountId());
            throw new AccountAlreadyExistsException(request.getAccountId());
        }

        try {
            // Create and save account
            Account account = new Account();
            account.setRefId(request.getAccountId());
            account.setBalance(request.getInitialBalance());

            accountRepository.save(account);
            log.info("Account {} created successfully with balance: {}", request.getAccountId(), request.getInitialBalance());
            
            return new AccountCreationResponse(
                    account.getRefId(),
                    account.getBalance());
        } catch (Exception e) {
            log.error("Failed to create account {}: {}", request.getAccountId(), e.getMessage());
            throw new RuntimeException("Failed to create account due to database error: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountId) {
        log.debug("Retrieving account with ID: {}", accountId);

        Optional<Account> accountOpt = accountRepository.findByRefId(accountId);

        if (accountOpt.isEmpty()) {
            log.warn("Account with ID {} not found", accountId);
            throw new AccountNotFoundException(accountId.toString());
        }

        Account account = accountOpt.get();
        log.debug("Account {} retrieved successfully with balance: {}", accountId, account.getBalance());

        return new AccountResponse(
                account.getRefId(),
                account.getBalance()
        );
    }
}
