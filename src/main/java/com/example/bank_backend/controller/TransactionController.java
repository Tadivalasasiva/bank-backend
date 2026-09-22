package com.example.bank_backend.controller;

import com.example.bank_backend.dto.TransactionResponse;
import com.example.bank_backend.entity.Transaction;
import com.example.bank_backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*; // Ensure CrossOrigin is imported

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "*") // <--- ADD THIS ANNOTATION
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/statement")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        List<Transaction> transactions = transactionService.getMyTransactions(username);

        List<TransactionResponse> responseList = transactions.stream().map(tx -> {
            TransactionResponse dto = new TransactionResponse();
            dto.setId(tx.getId());
            dto.setTransactionId(tx.getTransactionId());
            dto.setDescription(tx.getDescription());
            dto.setAmount(tx.getAmount());
            dto.setBalanceAfter(tx.getBalanceAfter());
            dto.setDate(tx.getTimestamp());
            dto.setType(tx.getType() != null ? tx.getType().name() : null);

            if (tx.getAccount() != null) {
                dto.setAccountNumber(tx.getAccount().getAccountNumber());
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}