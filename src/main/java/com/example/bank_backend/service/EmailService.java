package com.example.bank_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@nexorabank.com}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            logger.info("Email successfully sent to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Could not dispatch authentication email. Please try again.");
        }
    }

    // 1. Registration OTP Email
    public void sendRegistrationOtpEmail(String toEmail, String customerName, String otp) {
        String subject = "Nexora Financial - Registration Verification Code";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 580px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #082046; padding: 24px; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 20px; letter-spacing: 0.5px;">NEXORA FINANCIAL CORPORATION</h1>
                    <p style="color: #94a3b8; margin: 6px 0 0 0; font-size: 13px;">New Customer Onboarding</p>
                </div>
                <div style="padding: 30px 24px; background-color: #ffffff;">
                    <p style="font-size: 15px; color: #1e293b; margin-top: 0;">Dear <strong>%s</strong>,</p>
                    <p style="font-size: 14px; color: #475569; line-height: 1.5;">Thank you for choosing Nexora Financial Corporation. Please verify your email address to complete your registration by using the One-Time Password (OTP) below:</p>
                    <div style="margin: 28px 0; text-align: center;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1a56db; background: #f1f5f9; padding: 12px 28px; border-radius: 6px; border: 1px dashed #cbd5e1; display: inline-block;">%s</span>
                    </div>
                    <p style="font-size: 13px; color: #64748b; line-height: 1.4;">This OTP is valid for <strong>5 minutes</strong>. Never share this code with anyone, including bank representatives.</p>
                </div>
                <div style="background-color: #f8fafc; padding: 16px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;">
                    &copy; 2026 Nexora Financial Corporation. All rights reserved.
                </div>
            </div>
            """.formatted(customerName != null ? customerName : "Valued Customer", otp);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    // 2. Login OTP Email
    public void sendLoginOtpEmail(String toEmail, String customerName, String otp) {
        String subject = "Nexora Financial - Login Identity Verification";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 580px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #082046; padding: 24px; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 20px; letter-spacing: 0.5px;">NEXORA FINANCIAL CORPORATION</h1>
                    <p style="color: #94a3b8; margin: 6px 0 0 0; font-size: 13px;">Secure Internet Banking Gateway</p>
                </div>
                <div style="padding: 30px 24px; background-color: #ffffff;">
                    <p style="font-size: 15px; color: #1e293b; margin-top: 0;">Dear <strong>%s</strong>,</p>
                    <p style="font-size: 14px; color: #475569; line-height: 1.5;">A login attempt was initiated for your Nexora banking profile. Enter the following One-Time Password to authorize access:</p>
                    <div style="margin: 28px 0; text-align: center;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1a56db; background: #f1f5f9; padding: 12px 28px; border-radius: 6px; border: 1px dashed #cbd5e1; display: inline-block;">%s</span>
                    </div>
                    <p style="font-size: 13px; color: #64748b; line-height: 1.4;">Valid for <strong>5 minutes</strong>. If this wasn't you, your account may be compromised. Please lock your profile or contact Nexora Fraud Support immediately.</p>
                </div>
                <div style="background-color: #f8fafc; padding: 16px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;">
                    &copy; 2026 Nexora Financial Corporation. All rights reserved.
                </div>
            </div>
            """.formatted(customerName != null ? customerName : "Valued Customer", otp);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    // 3. Password Reset OTP Email (3 arguments)
    public void sendForgotPasswordOtpEmail(String toEmail, String customerName, String otp) {
        String subject = "Nexora Financial - Password Reset OTP";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 580px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #082046; padding: 24px; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 20px; letter-spacing: 0.5px;">NEXORA FINANCIAL CORPORATION</h1>
                    <p style="color: #94a3b8; margin: 6px 0 0 0; font-size: 13px;">Security & Account Recovery Services</p>
                </div>
                <div style="padding: 30px 24px; background-color: #ffffff;">
                    <p style="font-size: 15px; color: #1e293b; margin-top: 0;">Dear <strong>%s</strong>,</p>
                    <p style="font-size: 14px; color: #475569; line-height: 1.5;">We received a request to reset your password for your Nexora Internet Banking account. Please use the One-Time Password (OTP) below to proceed:</p>
                    <div style="margin: 28px 0; text-align: center;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1a56db; background: #f1f5f9; padding: 12px 28px; border-radius: 6px; border: 1px dashed #cbd5e1; display: inline-block;">%s</span>
                    </div>
                    <p style="font-size: 13px; color: #64748b; line-height: 1.4;">This code is strictly confidential and is valid for <strong>5 minutes</strong>. If you did not request a password reset, please contact Nexora 24/7 Fraud Support immediately.</p>
                </div>
                <div style="background-color: #f8fafc; padding: 16px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;">
                    &copy; 2026 Nexora Financial Corporation. All rights reserved.
                </div>
            </div>
            """.formatted(customerName != null ? customerName : "Valued Customer", otp);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    // 4. Password Reset Token Email (2 arguments, used by OtpService.java)
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String subject = "Nexora Financial - Password Reset Request";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 580px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #082046; padding: 24px; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 20px; letter-spacing: 0.5px;">NEXORA FINANCIAL CORPORATION</h1>
                    <p style="color: #94a3b8; margin: 6px 0 0 0; font-size: 13px;">Security & Account Recovery Services</p>
                </div>
                <div style="padding: 30px 24px; background-color: #ffffff;">
                    <p style="font-size: 15px; color: #1e293b; margin-top: 0;">Dear Valued Customer,</p>
                    <p style="font-size: 14px; color: #475569; line-height: 1.5;">We received a request to reset your password for your Nexora Internet Banking account. Please use the reset token below to complete the process:</p>
                    <div style="margin: 28px 0; text-align: center;">
                        <span style="font-size: 18px; font-weight: bold; letter-spacing: 2px; color: #1a56db; background: #f1f5f9; padding: 12px 24px; border-radius: 6px; border: 1px dashed #cbd5e1; display: inline-block; word-break: break-all;">%s</span>
                    </div>
                    <p style="font-size: 13px; color: #64748b; line-height: 1.4;">This token is valid for <strong>15 minutes</strong>. If you did not initiate this request, please contact Nexora 24/7 Fraud Support immediately.</p>
                </div>
                <div style="background-color: #f8fafc; padding: 16px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;">
                    &copy; 2026 Nexora Financial Corporation. All rights reserved.
                </div>
            </div>
            """.formatted(resetToken);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    // 5. Username Recovery Email
    public void sendForgotUsernameEmail(String toEmail, String customerName, String username) {
        String subject = "Nexora Financial - Username Recovery";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 580px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #082046; padding: 24px; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 20px; letter-spacing: 0.5px;">NEXORA FINANCIAL CORPORATION</h1>
                    <p style="color: #94a3b8; margin: 6px 0 0 0; font-size: 13px;">Security & Account Recovery Services</p>
                </div>
                <div style="padding: 30px 24px; background-color: #ffffff;">
                    <p style="font-size: 15px; color: #1e293b; margin-top: 0;">Dear <strong>%s</strong>,</p>
                    <p style="font-size: 14px; color: #475569; line-height: 1.5;">Per your request, here is your registered Nexora Internet Banking username:</p>
                    <div style="margin: 28px 0; text-align: center;">
                        <span style="font-size: 22px; font-weight: bold; color: #1a56db; background: #f1f5f9; padding: 12px 28px; border-radius: 6px; border: 1px solid #cbd5e1; display: inline-block;">%s</span>
                    </div>
                    <p style="font-size: 13px; color: #64748b; line-height: 1.4;">You can use this username to sign in to your banking portal. For your protection, never share your account details with third parties.</p>
                </div>
                <div style="background-color: #f8fafc; padding: 16px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;">
                    &copy; 2026 Nexora Financial Corporation. All rights reserved.
                </div>
            </div>
            """.formatted(customerName != null ? customerName : "Valued Customer", username);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    // 6. Fund Transfer OTP Email
    public void sendTransferOtpEmail(String toEmail, String senderName, String otp, String recipientName, String last4Account, String amount, String date) {
        String subject = "Nexora Financial - Fund Transfer Authorization Code";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 580px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #082046; padding: 24px; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 20px; letter-spacing: 0.5px;">NEXORA FINANCIAL CORPORATION</h1>
                    <p style="color: #94a3b8; margin: 6px 0 0 0; font-size: 13px;">Transaction Authorization Services</p>
                </div>
                <div style="padding: 30px 24px; background-color: #ffffff;">
                    <p style="font-size: 15px; color: #1e293b; margin-top: 0;">Dear <strong>%s</strong>,</p>
                    <p style="font-size: 14px; color: #475569; line-height: 1.5;">You are initiating a fund transfer. Details of the transaction are summarized below:</p>
                    <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 14px 18px; margin: 18px 0; font-size: 13px;">
                        <p style="margin: 4px 0;"><strong>Transfer Amount:</strong> INR %s</p>
                        <p style="margin: 4px 0;"><strong>Beneficiary:</strong> %s</p>
                        <p style="margin: 4px 0;"><strong>Target Account:</strong> XXXXXXX%s</p>
                        <p style="margin: 4px 0;"><strong>Timestamp:</strong> %s</p>
                    </div>
                    <p style="font-size: 14px; color: #475569; line-height: 1.5;">To authorize this debit, please submit the following One-Time Password:</p>
                    <div style="margin: 24px 0; text-align: center;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1a56db; background: #f1f5f9; padding: 12px 28px; border-radius: 6px; border: 1px dashed #cbd5e1; display: inline-block;">%s</span>
                    </div>
                    <p style="font-size: 13px; color: #64748b; line-height: 1.4;">Valid for <strong>5 minutes</strong>. If you did not authorize this fund transfer, immediately contact customer care to freeze your card and accounts.</p>
                </div>
                <div style="background-color: #f8fafc; padding: 16px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;">
                    &copy; 2026 Nexora Financial Corporation. All rights reserved.
                </div>
            </div>
            """.formatted(senderName != null ? senderName : "Valued Customer", amount, recipientName, last4Account, date, otp);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }
}