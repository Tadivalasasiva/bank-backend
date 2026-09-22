package com.example.bank_backend.controller;

import com.example.bank_backend.dto.AccountResponse;
import com.example.bank_backend.dto.AccountStatusUpdateRequest;
import com.example.bank_backend.dto.TransactionResponse;
import com.example.bank_backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/accounts")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllAccounts(pageable));
    }

    @PatchMapping("/accounts/{accountNumber}/status")
    public ResponseEntity<String> updateAccountStatus(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountStatusUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateAccountStatus(accountNumber, request));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getGlobalTransactions(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getGlobalTransactions(pageable));
    }
}