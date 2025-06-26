package com.common.bank.service;

import com.common.bank.dto.TransactionRequest;
import com.common.bank.dto.TransactionResponse;
import com.common.bank.enums.TransactionStatus;
import com.common.bank.exception.AccountNotFoundException;
import com.common.bank.exception.InsufficientBalanceException;
import com.common.bank.exception.InvalidTransactionException;
import com.common.bank.exception.TransactionNotFoundException;
import com.common.bank.model.Account;
import com.common.bank.model.Transaction;
import com.common.bank.repository.AccountRepository;
import com.common.bank.repository.TransactionRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        // Generate unique transaction reference ID
        String transactionRefId = UUID.randomUUID().toString();
        log.info("Starting transaction processing with ID: {}", transactionRefId);

        try {
            // Validate transaction
            validateTransaction(request, transactionRefId);
            
            // Get accounts
            Account sourceAccount = getAccount(request.getSourceAccountId(), transactionRefId, "Source");
            Account destinationAccount = getAccount(request.getDestinationAccountId(), transactionRefId, "Destination");
            
            // Validate business rules
            validateBusinessRules(sourceAccount, destinationAccount, request.getAmount(), transactionRefId);
            
            // Process the transaction
            return executeTransaction(request, sourceAccount, destinationAccount, transactionRefId);
            
        } catch (InvalidTransactionException | InsufficientBalanceException e) {
            return handleBusinessRuleFailure(request, transactionRefId, e);
        } catch (AccountNotFoundException e) {
            return handleAccountNotFoundFailure(request, transactionRefId, e);
        } catch (Exception e) {
            return handleUnexpectedFailure(request, transactionRefId, e);
        }
    }
    
    private void validateTransaction(TransactionRequest request, String transactionRefId) {
        Double transactionAmount = request.getAmount();
        if (transactionAmount == null) {
            log.warn("Transaction {} failed - Amount cannot be null", transactionRefId);
            throw InvalidTransactionException.nullAmountException();
        }
        if (StringUtils.isEmpty(request.getSourceAccountId())) {
            log.warn("Transaction {} failed - Source account id cannot be null", transactionRefId);
            throw new AccountNotFoundException("Source account id cannot be null");
        }
        if (StringUtils.isEmpty(request.getDestinationAccountId())) {
            log.warn("Transaction {} failed - Destination account id cannot be null or empty", transactionRefId);
            throw new AccountNotFoundException("Destination account id cannot be null or empty");
        }
        if (transactionAmount <= 0) {
            log.warn("Transaction {} failed - Amount must be positive: {}", transactionRefId, request.getAmount());
            throw InvalidTransactionException.nonPositiveAmountException(request.getAmount());
        }
        log.debug("Transaction {} - Amount validated: {}", transactionRefId, transactionAmount);
    }
    
    private Account getAccount(String accountId, String transactionRefId, String accountType) {
        Optional<Account> accountOpt = accountRepository.findByRefId(accountId);
        if (accountOpt.isEmpty()) {
            log.warn("Transaction {} failed - {} account not found: {}", transactionRefId, accountType, accountId);
            throw new AccountNotFoundException(accountId);
        }
        Account account = accountOpt.get();
        log.debug("Transaction {} - {} account found: {} with balance: {}", 
                 transactionRefId, accountType, account.getRefId(), account.getBalance());
        return account;
    }
    
    private void validateBusinessRules(Account sourceAccount, Account destinationAccount, 
                                     Double amount, String transactionRefId) {
        // Check if source and destination accounts are different
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            log.warn("Transaction {} failed - Source and destination accounts are the same: {}", 
                    transactionRefId, sourceAccount.getRefId());
            throw InvalidTransactionException.sameAccountException();
        }
        
        // Check if source account has sufficient balance
        Double sourceBalance = sourceAccount.getBalance();
        if (sourceBalance < amount) {
            log.warn("Transaction {} failed - Insufficient balance. Available: {}, Required: {}", 
                    transactionRefId, sourceBalance, amount);
            throw new InsufficientBalanceException(sourceBalance, amount);
        }
    }
    
    private TransactionResponse executeTransaction(TransactionRequest request, Account sourceAccount, 
                                                 Account destinationAccount, String transactionRefId) {
        // Create transaction record
        Transaction transaction = createTransactionRecord(request, transactionRefId);
        
        // Update account balances
        updateAccountBalances(sourceAccount, destinationAccount, request.getAmount(), transactionRefId);
        
        // Mark transaction as completed
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        log.info("Transaction {} completed successfully", transactionRefId);
        
        return new TransactionResponse(
            transactionRefId,
            request.getSourceAccountId(),
            request.getDestinationAccountId(),
            request.getAmount(),
            TransactionStatus.COMPLETED.getValue(),
            null
        );
    }
    
    private Transaction createTransactionRecord(TransactionRequest request, String transactionRefId) {
        Transaction transaction = new Transaction();
        transaction.setRefId(transactionRefId);
        transaction.setSourceAccountRefId(request.getSourceAccountId());
        transaction.setDestinationAccountRefId(request.getDestinationAccountId());
        transaction.setAmount(request.getAmount());
        transaction.setStatus(TransactionStatus.PROCESSING);
        
        transactionRepository.save(transaction);
        log.info("Transaction {} created with PROCESSING status", transactionRefId);
        return transaction;
    }
    
    private void updateAccountBalances(Account sourceAccount, Account destinationAccount, 
                                     Double amount, String transactionRefId) {
        Double newSourceBalance = sourceAccount.getBalance() - amount;
        Double newDestinationBalance = destinationAccount.getBalance() + amount;
        
        sourceAccount.setBalance(newSourceBalance);
        destinationAccount.setBalance(newDestinationBalance);
        
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
        
        log.info("Transaction {} - Balances updated. Source: {} -> {}, Destination: {} -> {}", 
                transactionRefId, 
                sourceAccount.getBalance() + amount, newSourceBalance,
                destinationAccount.getBalance() - amount, newDestinationBalance);
    }
    
    private TransactionResponse handleBusinessRuleFailure(TransactionRequest request, String transactionRefId, 
                                                        RuntimeException e) {
        log.error("Transaction {} failed: {}", transactionRefId, e.getMessage());
        
        // Create and save failed transaction record
        Transaction failedTransaction = createFailedTransactionRecord(request, transactionRefId, e.getMessage());
        saveFailedTransaction(failedTransaction, transactionRefId);
        
        return new TransactionResponse(
            transactionRefId,
            request.getSourceAccountId(),
            request.getDestinationAccountId(),
            request.getAmount(),
            TransactionStatus.FAILED.getValue(),
            e.getMessage()
        );
    }
    
    private TransactionResponse handleAccountNotFoundFailure(TransactionRequest request, String transactionRefId, 
                                                           AccountNotFoundException e) {
        log.error("Transaction {} failed - Account not found: {}", transactionRefId, e.getMessage());
        
        // Don't save to database due to foreign key constraints
        return new TransactionResponse(
            transactionRefId,
            request.getSourceAccountId(),
            request.getDestinationAccountId(),
            request.getAmount(),
            TransactionStatus.FAILED.getValue(),
            e.getMessage()
        );
    }
    
    private TransactionResponse handleUnexpectedFailure(TransactionRequest request, String transactionRefId, 
                                                      Exception e) {
        log.error("Transaction {} failed: {}", transactionRefId, e.getMessage());
        
        String errorMessage = "Transaction processing error: " + e.getMessage();
        Transaction failedTransaction = createFailedTransactionRecord(request, transactionRefId, errorMessage);
        saveFailedTransaction(failedTransaction, transactionRefId);
        
        return new TransactionResponse(
            transactionRefId,
            request.getSourceAccountId(),
            request.getDestinationAccountId(),
            request.getAmount(),
            TransactionStatus.FAILED.getValue(),
            errorMessage
        );
    }
    
    private Transaction createFailedTransactionRecord(TransactionRequest request, String transactionRefId, 
                                                    String errorMessage) {
        Transaction failedTransaction = new Transaction();
        failedTransaction.setRefId(transactionRefId);
        failedTransaction.setSourceAccountRefId(request.getSourceAccountId());
        failedTransaction.setDestinationAccountRefId(request.getDestinationAccountId());
        failedTransaction.setAmount(request.getAmount());
        failedTransaction.setStatus(TransactionStatus.FAILED);
        failedTransaction.setErrorMessage(errorMessage);
        return failedTransaction;
    }
    
    private void saveFailedTransaction(Transaction failedTransaction, String transactionRefId) {
        try {
            transactionRepository.save(failedTransaction);
            log.info("Failed transaction {} saved with error: {}", transactionRefId, failedTransaction.getErrorMessage());
        } catch (Exception saveException) {
            log.error("Failed to save failed transaction {}: {}", transactionRefId, saveException.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        log.debug("Retrieving transaction with ID: {}", transactionId);
        
        Optional<Transaction> transactionOpt = transactionRepository.findByRefId(transactionId);
        
        if (transactionOpt.isEmpty()) {
            log.warn("Transaction with ID {} not found", transactionId);
            throw new TransactionNotFoundException(transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        log.debug("Transaction {} retrieved successfully with status: {}", transactionId, transaction.getStatus());
        
        return new TransactionResponse(
            transaction.getRefId(),
            transaction.getSourceAccountRefId(),
            transaction.getDestinationAccountRefId(),
            transaction.getAmount(),
            transaction.getStatus().getValue(),
            transaction.getErrorMessage()
        );
    }
    
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String transactionId) {
        log.debug("Retrieving transaction with ID: {}", transactionId);
        
        Optional<Transaction> transactionOpt = transactionRepository.findByRefId(transactionId);
        
        if (transactionOpt.isEmpty()) {
            log.warn("Transaction with ID {} not found", transactionId);
            throw new TransactionNotFoundException(transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        log.debug("Transaction {} retrieved successfully with status: {}", transactionId, transaction.getStatus());
        
        return new TransactionResponse(
            transaction.getRefId(),
            transaction.getSourceAccountRefId(),
            transaction.getDestinationAccountRefId(),
            transaction.getAmount(),
            transaction.getStatus().getValue(),
            transaction.getErrorMessage()
        );
    }
}
