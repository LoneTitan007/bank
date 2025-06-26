package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {
    
    @JsonProperty("account_id")
    @NotBlank(message = "Account ID cannot be null or empty")
    private String accountId;
    
    @JsonProperty("initial_balance")
    @NotNull(message = "Initial balance cannot be null")
    @Positive(message = "Initial balance must be positive")
    private Double initialBalance;
}
