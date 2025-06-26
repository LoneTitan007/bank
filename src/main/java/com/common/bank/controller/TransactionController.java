package com.common.bank.controller;

import com.common.bank.dto.TransactionRequest;
import com.common.bank.dto.TransactionResponse;
import com.common.bank.exception.TransactionNotFoundException;
import com.common.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.common.bank.enums.TransactionStatus.COMPLETED;
import static com.common.bank.enums.TransactionStatus.FAILED;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(@RequestBody @Valid TransactionRequest request) {
        TransactionResponse response = transactionService.processTransaction(request);
        
        // Check if transaction was successful or failed
        if (COMPLETED.name().equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (FAILED.name().equals(response.getStatus())) {
            // Return failed transaction details with 400 status
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            // For other statuses (PROCESSING, PENDING, etc.)
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/{transaction_id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable("transaction_id") String transactionId) {
        try {
            TransactionResponse response = transactionService.getTransaction(transactionId);
            return ResponseEntity.ok(response);
        } catch (TransactionNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
