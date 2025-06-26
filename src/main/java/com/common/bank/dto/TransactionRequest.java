package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    @JsonProperty("source_account_id")
    private String sourceAccountId;
    
    @JsonProperty("destination_account_id")
    private String destinationAccountId;
    
    @JsonProperty("amount")
    private Double amount;
}
