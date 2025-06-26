package com.common.bank.service;

import com.common.bank.dto.AccountCreateRequest;
import com.common.bank.dto.AccountCreationResponse;
import com.common.bank.dto.AccountResponse;
import com.common.bank.exception.AccountAlreadyExistsException;
import com.common.bank.exception.AccountNotFoundException;
import com.common.bank.exception.InvalidBalanceException;
import com.common.bank.model.Account;
import com.common.bank.repository.AccountRepository;
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
class AccountServiceUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountServiceUnderTest;

    private Account testAccount;
    private AccountCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setRefId("TEST001");
        testAccount.setBalance(1000.0);

        createRequest = new AccountCreateRequest("TEST001", 1000.0);
    }

    @Test
    void createAccount_Success() {
        // Given
        when(accountRepository.findByRefId("TEST001")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountCreationResponse response = accountServiceUnderTest.createAccount(createRequest);

        // Then
        assertNotNull(response);
        assertEquals("TEST001", response.getAccountId());
        assertEquals(1000.0, response.getInitialBalance());
        
        verify(accountRepository).findByRefId("TEST001");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_AccountAlreadyExists() {
        // Given
        when(accountRepository.findByRefId("TEST001")).thenReturn(Optional.of(testAccount));

        // When & Then
        AccountAlreadyExistsException exception = assertThrows(
            AccountAlreadyExistsException.class,
            () -> accountServiceUnderTest.createAccount(createRequest)
        );

        assertEquals("Account with ID TEST001 already exists", exception.getMessage());
        verify(accountRepository).findByRefId("TEST001");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_NegativeInitialBalance() {
        // Given
        AccountCreateRequest negativeRequest = new AccountCreateRequest("TEST002", -100.0);
        when(accountRepository.findByRefId("TEST002")).thenReturn(Optional.empty());

        // When & Then
        InvalidBalanceException exception = assertThrows(
            InvalidBalanceException.class,
            () -> accountServiceUnderTest.createAccount(negativeRequest)
        );

        verify(accountRepository).findByRefId("TEST002");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_ZeroInitialBalance() {
        // Given
        AccountCreateRequest zeroRequest = new AccountCreateRequest("TEST003", 0.0);
        when(accountRepository.findByRefId("TEST003")).thenReturn(Optional.empty());

        // When & Then
        InvalidBalanceException exception = assertThrows(
            InvalidBalanceException.class,
            () -> accountServiceUnderTest.createAccount(zeroRequest)
        );

        assertTrue(exception.getMessage().contains("Initial balance cannot be non positive"));
        verify(accountRepository).findByRefId("TEST003");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_DatabaseError() {
        // Given
        when(accountRepository.findByRefId("TEST001")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(
            Exception.class,
            () -> accountServiceUnderTest.createAccount(createRequest)
        );
    }

    @Test
    void getAccount_Success() {
        // Given
        when(accountRepository.findByRefId("TEST001")).thenReturn(Optional.of(testAccount));

        // When
        AccountResponse response = accountServiceUnderTest.getAccount("TEST001");

        // Then
        assertNotNull(response);
        assertEquals("TEST001", response.getAccountId());
        assertEquals(1000.0, response.getBalance());
        
        verify(accountRepository).findByRefId("TEST001");
    }

    @Test
    void getAccount_NotFound() {
        // Given
        when(accountRepository.findByRefId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        AccountNotFoundException exception = assertThrows(
            AccountNotFoundException.class,
            () -> accountServiceUnderTest.getAccount("INVALID")
        );

        assertEquals("Account with ID INVALID not found", exception.getMessage());
        verify(accountRepository).findByRefId("INVALID");
    }

    @Test
    void createAccount_LargeBalance() {
        // Given
        AccountCreateRequest largeRequest = new AccountCreateRequest("LARGE001", 999999.99);
        Account largeAccount = new Account("LARGE001", 999999.99);
        
        when(accountRepository.findByRefId("LARGE001")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(largeAccount);

        // When
        AccountCreationResponse response = accountServiceUnderTest.createAccount(largeRequest);

        // Then
        assertNotNull(response);
        assertEquals("LARGE001", response.getAccountId());
        assertEquals(999999.99, response.getInitialBalance());
    }

    @Test
    void createAccount_PositiveInitialBalance() {
        // Given
        AccountCreateRequest positiveRequest = new AccountCreateRequest("TEST004", 100.0);
        Account positiveAccount = new Account();
        positiveAccount.setRefId("TEST004");
        positiveAccount.setBalance(100.0);
        
        when(accountRepository.findByRefId("TEST004")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(positiveAccount);

        // When
        AccountCreationResponse response = accountServiceUnderTest.createAccount(positiveRequest);

        // Then
        assertNotNull(response);
        assertEquals("TEST004", response.getAccountId());
        assertEquals(100.0, response.getInitialBalance());
        
        verify(accountRepository).findByRefId("TEST004");
        verify(accountRepository).save(any(Account.class));
    }
}
