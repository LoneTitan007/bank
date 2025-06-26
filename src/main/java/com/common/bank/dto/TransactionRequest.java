package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
    @PositiveOrZero(message = "Transaction amount cannot be negative")
    private Double amount;
}
