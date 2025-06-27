package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    @JsonProperty("source_account_id")
    @NotBlank(message = "Source Account ID cannot be null or empty")
    private String sourceAccountId;
    
    @JsonProperty("destination_account_id")
    @NotBlank(message = "Destination Account ID cannot be null or empty")
    private String destinationAccountId;
    
    @JsonProperty("amount")
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Transaction amount cannot be non positive")
    private Double amount;
}
