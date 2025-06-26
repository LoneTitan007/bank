package com.common.bank.controller;

import com.common.bank.dto.TransactionRequest;
import com.common.bank.dto.TransactionResponse;
import com.common.bank.exception.AccountNotFoundException;
import com.common.bank.exception.TransactionNotFoundException;
import com.common.bank.service.TransactionService;
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
 * Unit tests for TransactionController
 * Tests REST endpoints for transaction processing operations
 */
@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionRequest validTransactionRequest;
    private TransactionResponse successfulTransactionResponse;
    private TransactionResponse failedTransactionResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        validTransactionRequest = new TransactionRequest();
        validTransactionRequest.setSourceAccountId("SRC_ACC_001");
        validTransactionRequest.setDestinationAccountId("DST_ACC_001");
        validTransactionRequest.setAmount(100.0);

        successfulTransactionResponse = new TransactionResponse();
        successfulTransactionResponse.setTransactionId("TXN_001");
        successfulTransactionResponse.setSourceAccountId("SRC_ACC_001");
        successfulTransactionResponse.setDestinationAccountId("DST_ACC_001");
        successfulTransactionResponse.setAmount(100.0);
        successfulTransactionResponse.setStatus("COMPLETED");

        failedTransactionResponse = new TransactionResponse();
        failedTransactionResponse.setTransactionId("TXN_002");
        failedTransactionResponse.setSourceAccountId("SRC_ACC_001");
        failedTransactionResponse.setDestinationAccountId("DST_ACC_001");
        failedTransactionResponse.setAmount(100.0);
        failedTransactionResponse.setStatus("FAILED");
        failedTransactionResponse.setErrorMessage("Insufficient balance in source account");
    }

    // ========== Transaction Processing Tests ==========

    @Test
    void processTransaction_Success_ShouldReturnCreated() throws Exception {
        // Given
        when(transactionService.processTransaction(any(TransactionRequest.class)))
                .thenReturn(successfulTransactionResponse);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transaction_id").value("TXN_001"))
                .andExpect(jsonPath("$.source_account_id").value("SRC_ACC_001"))
                .andExpect(jsonPath("$.destination_account_id").value("DST_ACC_001"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void processTransaction_InsufficientBalance_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId("TXN_INSUFFICIENT");
        response.setSourceAccountId("SRC_ACC_001");
        response.setDestinationAccountId("DST_ACC_001");
        response.setAmount(1000.0);
        response.setStatus("FAILED");
        response.setErrorMessage("Insufficient balance in source account. Available: 500.0, Required: 1000.0");

        when(transactionService.processTransaction(any(TransactionRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransactionRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.error_message").exists());
    }

    @Test
    void processTransaction_NegativeAmount_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setSourceAccountId("SRC_ACC");
        request.setDestinationAccountId("DST_ACC");
        request.setAmount(-100.0);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTransaction_ZeroAmount_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setSourceAccountId("SRC_ACC");
        request.setDestinationAccountId("DST_ACC");
        request.setAmount(0.0);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTransaction_SameSourceAndDestination_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setSourceAccountId("SAME_ACC");
        request.setDestinationAccountId("SAME_ACC");
        request.setAmount(100.0);

        TransactionResponse response = new TransactionResponse();
        response.setTransactionId("TXN_SAME");
        response.setSourceAccountId("SAME_ACC");
        response.setDestinationAccountId("SAME_ACC");
        response.setAmount(100.0);
        response.setStatus("FAILED");
        response.setErrorMessage("Source and destination accounts cannot be the same");

        when(transactionService.processTransaction(any(TransactionRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.error_message").value("Source and destination accounts cannot be the same"));
    }

    @Test
    void processTransaction_SourceAccountNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(transactionService.processTransaction(any(TransactionRequest.class)))
                .thenThrow(new AccountNotFoundException("SRC_ACC_001"));

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransactionRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void processTransaction_DestinationAccountNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(transactionService.processTransaction(any(TransactionRequest.class)))
                .thenThrow(new AccountNotFoundException("DST_ACC_001"));

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransactionRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void processTransaction_MissingSourceAccountId_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setDestinationAccountId("DST_ACC");
        request.setAmount(100.0);
        // sourceAccountId is null

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTransaction_MissingDestinationAccountId_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setSourceAccountId("SRC_ACC");
        request.setAmount(100.0);
        // destinationAccountId is null

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTransaction_MissingAmount_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setSourceAccountId("SRC_ACC");
        request.setDestinationAccountId("DST_ACC");
        // amount is null

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTransaction_EmptySourceAccountId_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setSourceAccountId("");
        request.setDestinationAccountId("DST_ACC");
        request.setAmount(100.0);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTransaction_EmptyDestinationAccountId_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest();
        request.setSourceAccountId("SRC_ACC");
        request.setDestinationAccountId("");
        request.setAmount(100.0);

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== Transaction Retrieval Tests ==========

    @Test
    void getTransactionById_Success() throws Exception {
        // Given
        when(transactionService.getTransaction(eq("TXN_001")))
                .thenReturn(successfulTransactionResponse);

        // When & Then
        mockMvc.perform(get("/transactions/TXN_001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transaction_id").value("TXN_001"))
                .andExpect(jsonPath("$.source_account_id").value("SRC_ACC_001"))
                .andExpect(jsonPath("$.destination_account_id").value("DST_ACC_001"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getTransactionById_FailedTransaction() throws Exception {
        // Given
        when(transactionService.getTransaction(eq("TXN_002")))
                .thenReturn(failedTransactionResponse);

        // When & Then
        mockMvc.perform(get("/transactions/TXN_002"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transaction_id").value("TXN_002"))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.error_message").exists());
    }

    @Test
    void getTransactionById_NotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(transactionService.getTransaction(eq("NON_EXISTENT")))
                .thenThrow(new TransactionNotFoundException("NON_EXISTENT"));

        // When & Then
        mockMvc.perform(get("/transactions/NON_EXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionById_EmptyTransactionId_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/transactions/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionById_SpecialCharactersInTransactionId() throws Exception {
        // Given
        String specialTransactionId = "TXN_123-456_TEST";
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(specialTransactionId);
        response.setSourceAccountId("SRC_ACC");
        response.setDestinationAccountId("DST_ACC");
        response.setAmount(250.0);
        response.setStatus("COMPLETED");

        when(transactionService.getTransaction(eq(specialTransactionId)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/transactions/" + specialTransactionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transaction_id").value(specialTransactionId))
                .andExpect(jsonPath("$.amount").value(250.0));
    }

    @Test
    void getTransactionById_ServiceThrowsRuntimeException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(transactionService.getTransaction(eq("TXN_001")))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/transactions/TXN_001"))
                .andExpect(status().isInternalServerError());
    }

    // ========== Error Handling Tests ==========

    @Test
    void processTransaction_ServiceThrowsRuntimeException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(transactionService.processTransaction(any(TransactionRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransactionRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

    // ========== Content Type Tests ==========

    @Test
    void processTransaction_UnsupportedMediaType_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void processTransaction_MissingContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/transactions")
                        .content(objectMapper.writeValueAsString(validTransactionRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void processTransaction_InvalidJsonFormat_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidJson = "{ \"source_account_id\": \"SRC\", \"amount\": \"invalid\" }";

        // When & Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transactions_UnsupportedHttpMethod_ShouldReturnMethodNotAllowed() throws Exception {
        // When & Then
        mockMvc.perform(get("/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
    }

}
