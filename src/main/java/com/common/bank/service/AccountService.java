package com.common.bank.service;

import com.common.bank.dto.AccountCreateRequest;
import com.common.bank.dto.AccountResponse;
import com.common.bank.model.Account;
import com.common.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    public void createAccount(AccountCreateRequest request) {
        // Check if account already exists
        if (accountRepository.findByRefId(request.getAccountId().toString()).isPresent()) {
            throw new IllegalArgumentException("Account with ID " + request.getAccountId() + " already exists");
        }
        
        // Validate balance
        try {
            BigDecimal balance = new BigDecimal(request.getInitialBalance());
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Initial balance cannot be negative");
            }
            
            // Create and save account
            Account account = new Account();
            account.setRefId(request.getAccountId().toString());
            account.setBalance(balance.doubleValue());
            
            accountRepository.save(account);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid balance format: " + request.getInitialBalance());
        }
    }
    
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findByRefId(accountId.toString());
        
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account with ID " + accountId + " not found");
        }
        
        Account account = accountOpt.get();
        return new AccountResponse(
            Long.valueOf(account.getRefId()),
            account.getBalance().toString()
        );
    }
}
