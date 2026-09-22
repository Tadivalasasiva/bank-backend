package com.example.bank_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AccountStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(PENDING|ACTIVE|SUSPENDED|BLOCKED)$", message = "Invalid status. Allowed values: PENDING, ACTIVE, SUSPENDED, BLOCKED")
    private String status;

    public AccountStatusUpdateRequest() {}

    public AccountStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}