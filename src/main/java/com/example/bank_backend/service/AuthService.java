package com.example.bank_backend.service;

import com.example.bank_backend.dto.AuthResponse;
import com.example.bank_backend.dto.LoginRequest;
import com.example.bank_backend.dto.RegisterRequest;
import com.example.bank_backend.entity.Account;
import com.example.bank_backend.entity.AccountStatus;
import com.example.bank_backend.entity.Role;
import com.example.bank_backend.entity.User;
import com.example.bank_backend.repository.AccountRepository;
import com.example.bank_backend.repository.UserRepository;
import com.example.bank_backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    private static class OtpEntry {
        final String code;
        final LocalDateTime expiryTime;

        OtpEntry(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();

    @Autowired
    public AuthService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private String generateSixDigitOtp(String key) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(key.toLowerCase(), new OtpEntry(otp, LocalDateTime.now().plusMinutes(5)));
        return otp;
    }

    private boolean checkOtp(String key, String incomingOtp) {
        if (incomingOtp == null || incomingOtp.isBlank()) return false;
        OtpEntry entry = otpStorage.get(key.toLowerCase());
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiryTime)) {
            otpStorage.remove(key.toLowerCase());
            return false;
        }
        boolean isValid = entry.code.equals(incomingOtp.trim());
        if (isValid) otpStorage.remove(key.toLowerCase());
        return isValid;
    }

    private String createSignedJwtToken(String identifier) {
        try {
            var m1 = jwtTokenProvider.getClass().getMethod("generateToken", String.class);
            return (String) m1.invoke(jwtTokenProvider, identifier);
        } catch (Exception ignored) {}
        try {
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(identifier, null, java.util.Collections.emptyList());
            var m2 = jwtTokenProvider.getClass().getMethod("generateToken", org.springframework.security.core.Authentication.class);
            return (String) m2.invoke(jwtTokenProvider, auth);
        } catch (Exception ignored) {}
        throw new RuntimeException("JWT signing failed in JwtTokenProvider.");
    }

    private String extractLoginIdentifier(LoginRequest request) {
        try {
            var method = request.getClass().getMethod("getIdentifier");
            Object val = method.invoke(request);
            if (val != null && !val.toString().isBlank()) return val.toString();
        } catch (Exception ignored) {}
        try {
            var method = request.getClass().getMethod("getUsername");
            Object val = method.invoke(request);
            if (val != null && !val.toString().isBlank()) return val.toString();
        } catch (Exception ignored) {}
        try {
            var method = request.getClass().getMethod("getEmail");
            Object val = method.invoke(request);
            if (val != null && !val.toString().isBlank()) return val.toString();
        } catch (Exception ignored) {}
        return "";
    }

    private AuthResponse createAuthResponse(String token, String username) {
        try {
            return AuthResponse.class.getConstructor(String.class, String.class).newInstance(token, username);
        } catch (Exception ignored) {}
        try {
            return AuthResponse.class.getConstructor(String.class).newInstance(token);
        } catch (Exception ignored) {}
        try {
            AuthResponse resp = AuthResponse.class.getDeclaredConstructor().newInstance();
            try { AuthResponse.class.getMethod("setToken", String.class).invoke(resp, token); } catch (Exception ignored) {}
            try { AuthResponse.class.getMethod("setUsername", String.class).invoke(resp, username); } catch (Exception ignored) {}
            return resp;
        } catch (Exception e) {
            throw new RuntimeException("Could not construct AuthResponse", e);
        }
    }

    public Map<String, Object> requestRegisterOtp(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) throw new RuntimeException("Email already exists.");
        if (userRepository.findByUsername(request.getUsername()).isPresent()) throw new RuntimeException("Username is already taken.");
        String otp = generateSixDigitOtp("REG_" + request.getEmail());
        emailService.sendRegistrationOtpEmail(request.getEmail(), request.getFullName(), otp);
        return Map.of("message", "Registration OTP sent");
    }

    // DIRECT LOGIN IMPLEMENTATION (Validates password, no OTP)
    public AuthResponse login(LoginRequest request) {
        String identifier = extractLoginIdentifier(request);
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("Invalid credentials."));

        // Validate Password directly here!
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials.");
        }

        String token = createSignedJwtToken(user.getUsername());
        return createAuthResponse(token, user.getUsername());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public AuthResponse verifyOtpAndComplete(Map<String, Object> payload) {
        String flow = (String) payload.get("flow");
        String email = (String) payload.get("email");
        String otp = (String) payload.get("otp");
        Map<String, Object> dataPayload = (Map<String, Object>) payload.get("payload");

        String prefix = "REGISTRATION".equalsIgnoreCase(flow) ? "REG_" : "FORGOT_PW_";

        if (!checkOtp(prefix + email, otp)) throw new RuntimeException("Invalid OTP.");
        if ("FORGOT_PW".equalsIgnoreCase(flow)) return createAuthResponse("TEMP_RESET_AUTH", email);

        if ("REGISTRATION".equalsIgnoreCase(flow) && dataPayload != null) {
            User user = userRepository.findByEmail(email).orElse(new User());
            user.setEmail(email);
            user.setUsername((String) dataPayload.get("username"));
            user.setFullName((String) dataPayload.get("fullName"));
            user.setPhoneNumber((String) dataPayload.get("phoneNumber"));
            user.setPassword(passwordEncoder.encode((String) dataPayload.get("password")));
            user.setRole(Role.ROLE_CUSTOMER);
            user.setIsActive(true);
            user.setIsPhoneVerified(true);
            if (user.getCreatedAt() == null) user.setCreatedAt(LocalDateTime.now());
            user = userRepository.save(user);

            bindUserAccount(user, (String) dataPayload.get("accountNumber"));
            String token = createSignedJwtToken(user.getUsername());
            return createAuthResponse(token, user.getUsername());
        }

        throw new RuntimeException("Unsupported OTP flow.");
    }

    public Map<String, Object> sendForgotUsernameOtp(String accountNumber, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Account not found."));
        emailService.sendForgotUsernameEmail(email, user.getFullName(), user.getUsername());
        return Map.of("message", "Username sent", "username", user.getUsername());
    }

    public Map<String, Object> sendForgotPasswordOtp(String username, String email) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(email))
                .orElseThrow(() -> new RuntimeException("Account not found."));
        String otp = generateSixDigitOtp("FORGOT_PW_" + email);
        emailService.sendForgotPasswordOtpEmail(email, user.getFullName(), otp);
        return Map.of("message", "OTP sent.");
    }

    @Transactional
    public Map<String, Object> resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return Map.of("message", "Password reset successfully.");
    }

    private void bindUserAccount(User user, String accountNumber) {
        String finalAccountNumber = (accountNumber != null && !accountNumber.isBlank())
                ? accountNumber
                : "10" + (System.currentTimeMillis() % 1000000000L);

        Account account = accountRepository.findByUser(user)
                .orElseGet(() -> accountRepository.findByAccountNumber(finalAccountNumber).orElse(new Account()));

        account.setUser(user);
        account.setAccountNumber(finalAccountNumber);
        // Requirement 3: Strictly enforce ₹5,000 initial balance.
        if (account.getBalance() == null || account.getBalance() == 0.0) {
            account.setBalance(5000.00);
        }
        account.setStatus(AccountStatus.ACTIVE);
        if (account.getCreatedAt() == null) {
            account.setCreatedAt(LocalDateTime.now());
        }
        accountRepository.save(account);
    }
}