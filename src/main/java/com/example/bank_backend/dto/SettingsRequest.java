package com.example.bank_backend.dto;

public class SettingsRequest {

    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean twoFactorEnabled;

    public SettingsRequest() {}

    public SettingsRequest(boolean emailNotifications, boolean smsNotifications, boolean twoFactorEnabled) {
        this.emailNotifications = emailNotifications;
        this.smsNotifications = smsNotifications;
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public boolean isSmsNotifications() { return smsNotifications; }
    public void setSmsNotifications(boolean smsNotifications) { this.smsNotifications = smsNotifications; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
}