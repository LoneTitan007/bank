package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationResponse {
    
    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("initial_balance")
    private Double initialBalance;
}
