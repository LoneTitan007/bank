package com.common.bank.service;

import com.common.bank.dto.TransactionRequest;
import com.common.bank.dto.TransactionResponse;
import com.common.bank.enums.TransactionStatus;
import com.common.bank.model.Account;
import com.common.bank.model.Transaction;
import com.common.bank.repository.AccountRepository;
import com.common.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    public TransactionResponse processTransaction(TransactionRequest request) {
        // Generate unique transaction reference ID
        String transactionRefId = UUID.randomUUID().toString();
        
        try {
            // Validate transaction amount
            BigDecimal transactionAmount;
            try {
                transactionAmount = new BigDecimal(request.getAmount().toString());
                if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Transaction amount must be positive");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid amount format: " + request.getAmount());
            }
            
            // Find source account
            Optional<Account> sourceAccountOpt = accountRepository.findByRefId(request.getSourceAccountId());
            if (sourceAccountOpt.isEmpty()) {
                throw new IllegalArgumentException("Source account with ID " + request.getSourceAccountId() + " not found");
            }
            Account sourceAccount = sourceAccountOpt.get();
            
            // Find destination account
            Optional<Account> destinationAccountOpt = accountRepository.findByRefId(request.getDestinationAccountId());
            if (destinationAccountOpt.isEmpty()) {
                throw new IllegalArgumentException("Destination account with ID " + request.getDestinationAccountId() + " not found");
            }
            Account destinationAccount = destinationAccountOpt.get();
            
            // Check if source and destination accounts are different
            if (sourceAccount.getId().equals(destinationAccount.getId())) {
                throw new IllegalArgumentException("Source and destination accounts cannot be the same");
            }
            
            // Check if source account has sufficient balance
            BigDecimal sourceBalance = new BigDecimal(sourceAccount.getBalance().toString());
            if (sourceBalance.compareTo(transactionAmount) < 0) {
                throw new IllegalArgumentException("Insufficient balance in source account. Available: " + sourceBalance + ", Required: " + transactionAmount);
            }
            
            // Create transaction record with PROCESSING status
            Transaction transaction = new Transaction();
            transaction.setRefId(transactionRefId);
            transaction.setSourceAccountRefId(sourceAccount.getRefId());
            transaction.setDestinationAccountRefId(destinationAccount.getRefId());
            transaction.setAmount(transactionAmount.doubleValue());
            transaction.setStatus(TransactionStatus.PROCESSING);
            
            // Save transaction with PROCESSING status first
            transactionRepository.save(transaction);
            log.info("Transaction {} created with PROCESSING status", transactionRefId);
            
            // Update account balances
            BigDecimal newSourceBalance = sourceBalance.subtract(transactionAmount);
            BigDecimal destinationBalance = new BigDecimal(destinationAccount.getBalance().toString());
            BigDecimal newDestinationBalance = destinationBalance.add(transactionAmount);
            
            sourceAccount.setBalance(newSourceBalance.doubleValue());
            destinationAccount.setBalance(newDestinationBalance.doubleValue());
            
            // Save updated accounts
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);
            log.info("Account balances updated for transaction {}", transactionRefId);
            
            // Update transaction status to COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            log.info("Transaction {} completed successfully", transactionRefId);
            
            // Return transaction response
            return new TransactionResponse(
                transactionRefId,
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                TransactionStatus.COMPLETED.getValue(),
                null
            );
            
        } catch (Exception e) {
            log.error("Transaction {} failed: {}", transactionRefId, e.getMessage());
            
            // If any error occurs, mark transaction as FAILED
            try {
                Optional<Transaction> failedTransactionOpt = transactionRepository.findByRefId(transactionRefId);
                if (failedTransactionOpt.isPresent()) {
                    Transaction failedTransaction = failedTransactionOpt.get();
                    failedTransaction.setStatus(TransactionStatus.FAILED);
                    failedTransaction.setErrorMessage(e.getMessage());
                    transactionRepository.save(failedTransaction);
                    log.info("Transaction {} marked as FAILED", transactionRefId);
                }
            } catch (Exception saveException) {
                // Log the exception but don't throw it to avoid masking the original error
                log.error("Failed to update transaction {} status to FAILED: {}", transactionRefId, saveException.getMessage());
            }
            
            // Re-throw the original exception
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        log.debug("Retrieving transaction with ID: {}", transactionId);
        
        Optional<Transaction> transactionOpt = transactionRepository.findByRefId(transactionId);
        
        if (transactionOpt.isEmpty()) {
            log.warn("Transaction with ID {} not found", transactionId);
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " not found");
        }
        
        Transaction transaction = transactionOpt.get();
        log.debug("Transaction {} retrieved successfully", transactionId);
        
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
