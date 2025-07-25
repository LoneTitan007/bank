package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    
    @JsonProperty("account_id")
    private String accountId;
    
    @JsonProperty("balance")
    private Double balance;
}
