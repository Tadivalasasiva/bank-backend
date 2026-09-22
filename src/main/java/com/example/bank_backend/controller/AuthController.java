package com.example.bank_backend.controller;

import com.example.bank_backend.dto.AuthResponse;
import com.example.bank_backend.dto.LoginRequest;
import com.example.bank_backend.dto.RegisterRequest;
import com.example.bank_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Registration (Still uses OTP)
    @PostMapping("/register/request-otp")
    public ResponseEntity<?> requestRegisterOtp(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> response = authService.requestRegisterOtp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Direct Login (Bypasses OTP)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Verify OTP for Registration and Password Resets
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> payload) {
        try {
            AuthResponse authResponse = authService.verifyOtpAndComplete(payload);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Password & Username Reset endpoints
    @PostMapping("/forgot-username")
    public ResponseEntity<?> forgotUsername(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.sendForgotUsernameOtp(body.get("accountNumber"), body.get("email")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.sendForgotPasswordOtp(body.get("username"), body.get("email")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.resetPassword(body.get("email"), body.get("newPassword")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}