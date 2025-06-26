package com.common.bank.service;

import com.common.bank.dto.TransactionRequest;
import com.common.bank.dto.TransactionResponse;
import com.common.bank.exception.TransactionNotFoundException;
import com.common.bank.model.Account;
import com.common.bank.model.Transaction;
import com.common.bank.enums.TransactionStatus;
import com.common.bank.repository.AccountRepository;
import com.common.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account destinationAccount;
    private Transaction testTransaction;
    private TransactionRequest validRequest;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setRefId("SRC001");
        sourceAccount.setBalance(1000.0);

        destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setRefId("DST001");
        destinationAccount.setBalance(500.0);

        testTransaction = new Transaction();
        testTransaction.setRefId("TXN001");
        testTransaction.setSourceAccountRefId("SRC001");
        testTransaction.setDestinationAccountRefId("DST001");
        testTransaction.setAmount(300.0);
        testTransaction.setStatus(TransactionStatus.PROCESSING);

        validRequest = new TransactionRequest("SRC001", "DST001", 300.0);
    }

    @Test
    void processTransaction_Success() {
        // Given
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByRefId("DST001")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse response = transactionService.processTransaction(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("SRC001", response.getSourceAccountId());
        assertEquals("DST001", response.getDestinationAccountId());
        assertEquals(300.0, response.getAmount());
        assertEquals("COMPLETED", response.getStatus());
        assertNull(response.getErrorMessage());

        // Verify balance updates
        assertEquals(700.0, sourceAccount.getBalance());
        assertEquals(800.0, destinationAccount.getBalance());

        verify(accountRepository, times(2)).findByRefId(any());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void processTransaction_NegativeAmount() {
        // Given
        TransactionRequest negativeRequest = new TransactionRequest("SRC001", "DST001", -100.0);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse response = transactionService.processTransaction(negativeRequest);

        // Then
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getErrorMessage().contains("Transaction amount must be positive"));

        verify(accountRepository, never()).findByRefId(any());
        verify(transactionRepository, times(1)).save(any(Transaction.class)); // Failed transaction is saved
    }

    @Test
    void processTransaction_ZeroAmount() {
        // Given
        TransactionRequest zeroRequest = new TransactionRequest("SRC001", "DST001", 0.0);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse response = transactionService.processTransaction(zeroRequest);

        // Then
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getErrorMessage().contains("Transaction amount must be positive"));
        
        verify(transactionRepository, times(1)).save(any(Transaction.class)); // Failed transaction is saved
    }

    @Test
    void processTransaction_SourceAccountNotFound() {
        // Given
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.empty());

        // When
        TransactionResponse response = transactionService.processTransaction(validRequest);

        // Then
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getErrorMessage().contains("Account with ID SRC001 not found"));

        verify(accountRepository).findByRefId("SRC001");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processTransaction_DestinationAccountNotFound() {
        // Given
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByRefId("DST001")).thenReturn(Optional.empty());

        // When
        TransactionResponse response = transactionService.processTransaction(validRequest);

        // Then
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getErrorMessage().contains("Account with ID DST001 not found"));

        verify(accountRepository).findByRefId("SRC001");
        verify(accountRepository).findByRefId("DST001");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processTransaction_InsufficientBalance() {
        // Given
        TransactionRequest largeRequest = new TransactionRequest("SRC001", "DST001", 1500.0);
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByRefId("DST001")).thenReturn(Optional.of(destinationAccount));

        // When
        TransactionResponse response = transactionService.processTransaction(largeRequest);

        // Then
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getErrorMessage().contains("Insufficient balance"));
        assertTrue(response.getErrorMessage().contains("Available: 1000.0"));
        assertTrue(response.getErrorMessage().contains("Required: 1500.0"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransaction_SameAccount() {
        // Given
        TransactionRequest sameAccountRequest = new TransactionRequest("SRC001", "SRC001", 300.0);
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.of(sourceAccount));

        // When
        TransactionResponse response = transactionService.processTransaction(sameAccountRequest);

        // Then
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getErrorMessage().contains("Source and destination accounts cannot be the same"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransaction_ExactBalance() {
        // Given
        TransactionRequest exactRequest = new TransactionRequest("SRC001", "DST001", 1000.0);
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByRefId("DST001")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse response = transactionService.processTransaction(exactRequest);

        // Then
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(0.0, sourceAccount.getBalance());
        assertEquals(1500.0, destinationAccount.getBalance());
    }

    @Test
    void processTransaction_SmallAmount() {
        // Given
        TransactionRequest smallRequest = new TransactionRequest("SRC001", "DST001", 0.01);
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByRefId("DST001")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse response = transactionService.processTransaction(smallRequest);

        // Then
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(999.99, sourceAccount.getBalance(), 0.001);
        assertEquals(500.01, destinationAccount.getBalance(), 0.001);
    }

    @Test
    void processTransaction_DatabaseError() {
        // Given
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByRefId("DST001")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        TransactionResponse response = transactionService.processTransaction(validRequest);

        // Then
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getErrorMessage().contains("Transaction processing error"));
        assertTrue(response.getErrorMessage().contains("Database error"));
    }

    @Test
    void getTransactionById_Success() {
        // Given
        Transaction completedTransaction = new Transaction();
        completedTransaction.setRefId("TXN123");
        completedTransaction.setSourceAccountRefId("SRC001");
        completedTransaction.setDestinationAccountRefId("DST001");
        completedTransaction.setAmount(250.0);
        completedTransaction.setStatus(TransactionStatus.COMPLETED);

        when(transactionRepository.findByRefId("TXN123")).thenReturn(Optional.of(completedTransaction));

        // When
        TransactionResponse response = transactionService.getTransactionById("TXN123");

        // Then
        assertNotNull(response);
        assertEquals("TXN123", response.getTransactionId());
        assertEquals("SRC001", response.getSourceAccountId());
        assertEquals("DST001", response.getDestinationAccountId());
        assertEquals(250.0, response.getAmount());
        assertEquals("COMPLETED", response.getStatus());

        verify(transactionRepository).findByRefId("TXN123");
    }

    @Test
    void getTransactionById_NotFound() {
        // Given
        when(transactionRepository.findByRefId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        TransactionNotFoundException exception = assertThrows(
            TransactionNotFoundException.class,
            () -> transactionService.getTransactionById("INVALID")
        );

        assertEquals("Transaction with ID INVALID not found", exception.getMessage());
        verify(transactionRepository).findByRefId("INVALID");
    }

    @Test
    void getTransactionById_FailedTransaction() {
        // Given
        Transaction failedTransaction = new Transaction();
        failedTransaction.setRefId("TXN_FAILED");
        failedTransaction.setSourceAccountRefId("SRC001");
        failedTransaction.setDestinationAccountRefId("DST001");
        failedTransaction.setAmount(1500.0);
        failedTransaction.setStatus(TransactionStatus.FAILED);
        failedTransaction.setErrorMessage("Insufficient balance");

        when(transactionRepository.findByRefId("TXN_FAILED")).thenReturn(Optional.of(failedTransaction));

        // When
        TransactionResponse response = transactionService.getTransactionById("TXN_FAILED");

        // Then
        assertEquals("TXN_FAILED", response.getTransactionId());
        assertEquals("FAILED", response.getStatus());
        assertEquals("Insufficient balance", response.getErrorMessage());
    }

    @Test
    void processTransaction_VerifyMethodCalls() {
        // Given
        when(accountRepository.findByRefId("SRC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByRefId("DST001")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        transactionService.processTransaction(validRequest);

        // Then
        var inOrder = inOrder(accountRepository, transactionRepository);
        inOrder.verify(accountRepository).findByRefId("SRC001");
        inOrder.verify(accountRepository).findByRefId("DST001");
        inOrder.verify(transactionRepository).save(any(Transaction.class)); // PROCESSING
        inOrder.verify(accountRepository).save(sourceAccount);
        inOrder.verify(accountRepository).save(destinationAccount);
        inOrder.verify(transactionRepository).save(any(Transaction.class)); // COMPLETED
    }
}
