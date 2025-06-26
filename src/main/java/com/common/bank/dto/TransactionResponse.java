package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("source_account_id")
    private String sourceAccountId;
    
    @JsonProperty("destination_account_id")
    private String destinationAccountId;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("error_message")
    private String errorMessage = null;

}
