package com.example.bank_backend.controller;

import com.example.bank_backend.dto.ChangePasswordRequest;
import com.example.bank_backend.dto.SetMpinRequest;
import com.example.bank_backend.dto.SettingsRequest;
import com.example.bank_backend.security.UserPrincipal;
import com.example.bank_backend.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(settingsService.changePassword(currentUser.getId(), request));
    }

    @PostMapping("/mpin")
    public ResponseEntity<String> setMpin(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SetMpinRequest request) {
        return ResponseEntity.ok(settingsService.setMpin(currentUser.getId(), request));
    }

    @PutMapping("/preferences")
    public ResponseEntity<String> updatePreferences(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SettingsRequest request) {
        return ResponseEntity.ok(settingsService.updatePreferences(currentUser.getId(), request));
    }
}