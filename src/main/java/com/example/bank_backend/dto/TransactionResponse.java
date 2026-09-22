package com.example.bank_backend.dto;

import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String transactionId; // Used for reference tracking
    private String description;
    private Double amount;
    private Double balanceAfter;
    private LocalDateTime date;
    private String type; // "DEBIT" or "CREDIT"

    // REPLACED fromAccount and toAccount with a single account owner field
    private String accountNumber;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Double balanceAfter) { this.balanceAfter = balanceAfter; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}