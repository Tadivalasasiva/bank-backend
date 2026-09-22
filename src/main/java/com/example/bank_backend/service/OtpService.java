package com.example.bank_backend.service;

import com.example.bank_backend.dto.ForgotPasswordRequest;
import com.example.bank_backend.dto.ResetPasswordRequest;
import com.example.bank_backend.dto.VerifyOtpRequest;
import com.example.bank_backend.entity.AccountStatus;
import com.example.bank_backend.entity.OtpVerification;
import com.example.bank_backend.entity.PasswordResetToken;
import com.example.bank_backend.entity.User;
import com.example.bank_backend.repository.AccountRepository;
import com.example.bank_backend.repository.OtpVerificationRepository;
import com.example.bank_backend.repository.PasswordResetTokenRepository;
import com.example.bank_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpVerificationRepository otpRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public OtpService(OtpVerificationRepository otpRepository,
                      PasswordResetTokenRepository resetTokenRepository,
                      UserRepository userRepository,
                      AccountRepository accountRepository,
                      EmailService emailService,
                      PasswordEncoder passwordEncoder) {
        this.otpRepository = otpRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void generateAndSendOtp(String phoneNumber) {
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        OtpVerification verification = new OtpVerification(phoneNumber, otp, LocalDateTime.now().plusMinutes(5));
        otpRepository.save(verification);

        // Dispatches via SMS or local mock output
        log.info("[SMS OTP SERVICE] Verification code sent to {}: {}", phoneNumber, otp);
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest request) {
        OtpVerification verification = otpRepository
                .findTopByPhoneNumberAndIsUsedFalseOrderByExpiryTimeDesc(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("No active OTP request found for this phone number."));

        if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        if (!verification.getOtpCode().equals(request.getOtpCode())) {
            throw new RuntimeException("Invalid OTP code provided.");
        }

        verification.setUsed(true);
        otpRepository.save(verification);

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("User not registered with this number."));
        user.setPhoneVerified(true);
        userRepository.save(user);

        // Activate customer's account
        accountRepository.findByUserId(user.getId()).ifPresent(acc -> {
            acc.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(acc);
        });

        return "Phone number verified successfully. Account is now active.";
    }

    @Transactional
    public String initiatePasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User with this email not found."));

        resetTokenRepository.deleteByUser_Id(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(15));
        resetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
        return "Password reset token dispatched to your email.";
    }

    @Transactional
    public String completePasswordReset(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or non-existent reset token."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken);
        return "Password updated successfully.";
    }
}