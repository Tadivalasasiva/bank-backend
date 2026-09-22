package com.example.bank_backend.service;

import com.example.bank_backend.dto.ChangePasswordRequest;
import com.example.bank_backend.dto.SetMpinRequest;
import com.example.bank_backend.dto.SettingsRequest;
import com.example.bank_backend.entity.User;
import com.example.bank_backend.entity.UserSettings;
import com.example.bank_backend.repository.UserRepository;
import com.example.bank_backend.repository.UserSettingsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final UserRepository userRepository;
    private final UserSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingsService(UserRepository userRepository,
                           UserSettingsRepository settingsRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password does not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully.";
    }

    @Transactional
    public String setMpin(Long userId, SetMpinRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        UserSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> new UserSettings(user));

        settings.setMpin(passwordEncoder.encode(request.getMpin()));
        settingsRepository.save(settings);
        return "MPIN set successfully.";
    }

    @Transactional
    public String updatePreferences(Long userId, SettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        UserSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> new UserSettings(user));

        settings.setEmailNotifications(request.isEmailNotifications());
        settings.setSmsNotifications(request.isSmsNotifications());
        settings.setTwoFactorEnabled(request.isTwoFactorEnabled());
        settingsRepository.save(settings);
        return "Settings updated successfully.";
    }
}