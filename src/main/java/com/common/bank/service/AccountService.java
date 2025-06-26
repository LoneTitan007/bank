package com.common.bank.service;

import com.common.bank.dto.AccountCreateRequest;
import com.common.bank.dto.AccountCreationResponse;
import com.common.bank.dto.AccountResponse;
import com.common.bank.exception.AccountAlreadyExistsException;
import com.common.bank.exception.AccountNotFoundException;
import com.common.bank.exception.InvalidBalanceException;
import com.common.bank.model.Account;
import com.common.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import static com.common.bank.exception.InvalidBalanceException.nonPositiveBalanceException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountCreationResponse createAccount(AccountCreateRequest request) {
        log.info("Creating account with ID: {}", request.getAccountId());

        try {
            validateAccountCreationRequest(request);
            Account account = buildAccount(request);
            Account savedAccount = saveAccount(account);

            log.info("Account {} created successfully with balance: {}",
                    savedAccount.getRefId(), savedAccount.getBalance());

            return buildAccountCreationResponse(savedAccount);

        } catch (AccountAlreadyExistsException | InvalidBalanceException e) {
            log.warn("Account creation failed for {}: {}", request.getAccountId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to create account {}: {}", request.getAccountId(), e.getMessage());
            throw new RuntimeException("Failed to create account due to database error: " + e.getMessage(), e);
        }
    }

    private void validateAccountCreationRequest(AccountCreateRequest request) {
        log.debug("Validating account creation request for ID: {}", request.getAccountId());

        validateAccountDoesNotExist(request.getAccountId());
        validateInitialBalance(request.getInitialBalance());

        log.debug("Account creation request validation passed for ID: {}", request.getAccountId());
    }

    private void validateAccountDoesNotExist(String accountId) {
        if (accountRepository.findByRefId(accountId).isPresent()) {
            log.warn("Account creation failed - Account with ID {} already exists", accountId);
            throw new AccountAlreadyExistsException(accountId);
        }
    }

    private void validateInitialBalance(Double initialBalance) {
        if (initialBalance <= 0) {
            log.warn("Account creation failed - Non positive initial balance not allowed: {}", initialBalance);
            throw nonPositiveBalanceException(initialBalance);
        }
    }

    private Account buildAccount(AccountCreateRequest request) {
        log.debug("Building account entity for ID: {}", request.getAccountId());

        Account account = new Account();
        account.setRefId(request.getAccountId());
        account.setBalance(request.getInitialBalance());

        return account;
    }

    private Account saveAccount(Account account) {
        log.debug("Saving account to database: {}", account.getRefId());

        Account savedAccount = accountRepository.save(account);

        log.debug("Account saved successfully with ID: {}", savedAccount.getId());
        return savedAccount;
    }

    private AccountCreationResponse buildAccountCreationResponse(Account account) {
        return new AccountCreationResponse(
                account.getRefId(),
                account.getBalance()
        );
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
