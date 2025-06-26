package com.common.bank.controller;

import com.common.bank.dto.AccountCreateRequest;
import com.common.bank.dto.AccountCreationResponse;
import com.common.bank.dto.AccountResponse;
import com.common.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountCreationResponse> createAccount(@RequestBody @Valid AccountCreateRequest request) {
        AccountCreationResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{account_id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable("account_id") String accountId) {
        AccountResponse response = accountService.getAccount(accountId);
        return ResponseEntity.ok(response);

    }
}
