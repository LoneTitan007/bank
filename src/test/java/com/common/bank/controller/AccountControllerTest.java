package com.common.bank.controller;

import com.common.bank.dto.AccountCreateRequest;
import com.common.bank.dto.AccountCreationResponse;
import com.common.bank.dto.AccountResponse;
import com.common.bank.exception.AccountAlreadyExistsException;
import com.common.bank.exception.AccountNotFoundException;
import com.common.bank.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AccountController
 * Tests REST endpoints for account management operations
 */
@WebMvcTest(controllers = AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountCreateRequest validAccountRequest;
    private AccountCreationResponse accountCreationResponse;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        validAccountRequest = new AccountCreateRequest();
        validAccountRequest.setAccountId("TEST_ACC_001");
        validAccountRequest.setInitialBalance(1000.0);

        accountCreationResponse = new AccountCreationResponse();
        accountCreationResponse.setAccountId("TEST_ACC_001");
        accountCreationResponse.setInitialBalance(1000.0);

        accountResponse = new AccountResponse();
        accountResponse.setAccountId("TEST_ACC_001");
        accountResponse.setBalance(1000.0);
    }

    @Test
    void createAccount_Success() throws Exception {
        // Given
        when(accountService.createAccount(any(AccountCreateRequest.class)))
                .thenReturn(accountCreationResponse);

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAccountRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.account_id").value("TEST_ACC_001"))
                .andExpect(jsonPath("$.initial_balance").value(1000.0));
    }

    @Test
    void createAccount_WithPositiveBalance() throws Exception {
        // Given
        AccountCreateRequest request = new AccountCreateRequest();
        request.setAccountId("POSITIVE_ACC");
        request.setInitialBalance(500.50);

        AccountCreationResponse response = new AccountCreationResponse();
        response.setAccountId("POSITIVE_ACC");
        response.setInitialBalance(500.50);

        when(accountService.createAccount(any(AccountCreateRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value("POSITIVE_ACC"))
                .andExpect(jsonPath("$.initial_balance").value(500.50));
    }

    @Test
    void createAccount_NegativeBalance_ShouldReturnBadRequest() throws Exception {
        // Given
        AccountCreateRequest request = new AccountCreateRequest("NEG_ACC", -100.0);

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void createAccount_ZeroBalance_ShouldReturnBadRequest() throws Exception {
        // Given
        AccountCreateRequest request = new AccountCreateRequest("ZERO_ACC", 0.0);

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void createAccount_DuplicateAccount_ShouldReturnConflict() throws Exception {
        // Given
        when(accountService.createAccount(any(AccountCreateRequest.class)))
                .thenThrow(new AccountAlreadyExistsException("TEST_ACC_001"));

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAccountRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_ALREADY_EXISTS"));
    }

    @Test
    void createAccount_MissingAccountId_ShouldReturnBadRequest() throws Exception {
        // Given
        AccountCreateRequest request = new AccountCreateRequest();
        request.setInitialBalance(1000.0);
        // accountId is null

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_MissingInitialBalance_ShouldReturnBadRequest() throws Exception {
        // Given
        AccountCreateRequest request = new AccountCreateRequest();
        request.setAccountId("TEST_ACC");
        // initialBalance is null

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_EmptyAccountId_ShouldReturnBadRequest() throws Exception {
        // Given
        AccountCreateRequest request = new AccountCreateRequest();
        request.setAccountId("");
        request.setInitialBalance(1000.0);

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_InvalidJsonFormat_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidJson = "{ \"account_id\": \"TEST\", \"initial_balance\": \"invalid\" }";

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ========== Account Retrieval Tests ==========

    @Test
    void getAccount_Success() throws Exception {
        // Given
        when(accountService.getAccount(eq("TEST_ACC_001")))
                .thenReturn(accountResponse);

        // When & Then
        mockMvc.perform(get("/accounts/TEST_ACC_001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.account_id").value("TEST_ACC_001"))
                .andExpect(jsonPath("$.balance").value(1000.0));
    }

    @Test
    void getAccount_WithDifferentBalance() throws Exception {
        // Given
        AccountResponse response = new AccountResponse();
        response.setAccountId("DIFF_ACC");
        response.setBalance(2500.75);

        when(accountService.getAccount(eq("DIFF_ACC")))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/accounts/DIFF_ACC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value("DIFF_ACC"))
                .andExpect(jsonPath("$.balance").value(2500.75));
    }

    @Test
    void getAccount_ZeroBalance() throws Exception {
        // Given
        AccountResponse response = new AccountResponse();
        response.setAccountId("ZERO_BAL_ACC");
        response.setBalance(0.0);

        when(accountService.getAccount(eq("ZERO_BAL_ACC")))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/accounts/ZERO_BAL_ACC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value("ZERO_BAL_ACC"))
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    void getAccount_NotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(accountService.getAccount(eq("NON_EXISTENT")))
                .thenThrow(new AccountNotFoundException("NON_EXISTENT"));

        // When & Then
        mockMvc.perform(get("/accounts/NON_EXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void getAccount_EmptyAccountId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/accounts/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccount_SpecialCharactersInAccountId() throws Exception {
        // Given
        String specialAccountId = "ACC_123-456_TEST";
        AccountResponse response = new AccountResponse();
        response.setAccountId(specialAccountId);
        response.setBalance(1500.0);

        when(accountService.getAccount(eq(specialAccountId)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/accounts/" + specialAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(specialAccountId))
                .andExpect(jsonPath("$.balance").value(1500.0));
    }

    @Test
    void createAccount_ServiceThrowsRuntimeException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(accountService.createAccount(any(AccountCreateRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAccountRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void getAccount_ServiceThrowsRuntimeException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(accountService.getAccount(eq("TEST_ACC_001")))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/accounts/TEST_ACC_001"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

    // ========== Content Type Tests ==========

    @Test
    void createAccount_UnsupportedMediaType_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void createAccount_MissingContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/accounts")
                        .content(objectMapper.writeValueAsString(validAccountRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== HTTP Method Tests ==========

    @Test
    void accounts_UnsupportedHttpMethod_ShouldReturnMethodNotAllowed() throws Exception {
        // When & Then
        mockMvc.perform(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
    }
}
