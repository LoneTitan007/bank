package com.common.bank.controller;

import com.common.bank.dto.TransactionRequest;
import com.common.bank.dto.TransactionResponse;
import com.common.bank.service.TransactionService;
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
    public ResponseEntity<TransactionResponse> processTransaction(@RequestBody TransactionRequest request) {
        try {
            TransactionResponse response = transactionService.processTransaction(request);
            
            // Check if transaction was successful or failed
            if (COMPLETED.equals(response.getStatus())) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else if (FAILED.equals(response.getStatus())) {
                // Return failed transaction details with 400 status
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                // For other statuses (PROCESSING, PENDING, etc.)
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            }
            
        } catch (Exception e) {
            // This should rarely happen now since we record failed transactions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{transaction_id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable("transaction_id") String transactionId) {
        try {
            TransactionResponse response = transactionService.getTransaction(transactionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
