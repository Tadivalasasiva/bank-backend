package com.example.bank_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SetMpinRequest {

    @NotBlank(message = "MPIN is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "MPIN must be a 4 or 6 digit number")
    private String mpin;

    public SetMpinRequest() {}

    public SetMpinRequest(String mpin) {
        this.mpin = mpin;
    }

    public String getMpin() { return mpin; }
    public void setMpin(String mpin) { this.mpin = mpin; }
}