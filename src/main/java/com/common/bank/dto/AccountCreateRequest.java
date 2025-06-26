package com.common.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {
    
    @JsonProperty("account_id")
    @NotBlank(message = "Account ID cannot be null or empty")
    private String accountId;
    
    @JsonProperty("initial_balance")
    @NotNull(message = "Initial balance cannot be null")
    @PositiveOrZero(message = "Initial balance cannot be negative")
    private Double initialBalance;
}
