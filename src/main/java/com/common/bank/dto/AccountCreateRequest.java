package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {
    
    @JsonProperty("account_id")
    private Long accountId;
    
    @JsonProperty("initial_balance")
    private String initialBalance;
}
