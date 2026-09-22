package com.example.bank_backend.dto;

public class TransferRequest {

    private String fromAccountNumber;
    private String toAccountNumber;
    private String recipientAccountNumber;
    private Double amount;
    private String description;
    private String transferRequestId;
    private String otp;

    public TransferRequest() {
    }

    public TransferRequest(String fromAccountNumber, String toAccountNumber, Double amount, String description) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.recipientAccountNumber = toAccountNumber;
        this.amount = amount;
        this.description = description;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber != null ? toAccountNumber : recipientAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
        this.recipientAccountNumber = toAccountNumber;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber != null ? recipientAccountNumber : toAccountNumber;
    }

    public void setRecipientAccountNumber(String recipientAccountNumber) {
        this.recipientAccountNumber = recipientAccountNumber;
        this.toAccountNumber = recipientAccountNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransferRequestId() {
        return transferRequestId;
    }

    public void setTransferRequestId(String transferRequestId) {
        this.transferRequestId = transferRequestId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}