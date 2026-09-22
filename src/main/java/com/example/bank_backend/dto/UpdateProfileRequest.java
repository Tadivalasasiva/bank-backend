package com.example.bank_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {

    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}