package com.ajith.codejudge.notification.service.impl;

import com.ajith.codejudge.notification.service.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@codejudge.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = frontendUrl + "/auth/verify?token=" + token;
        String subject = "Verify your email - CodeJudge Pro";
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                + "<h2 style='color: #4F46E5; text-align: center;'>Welcome to CodeJudge Pro!</h2>"
                + "<p>Thank you for registering. Please click the button below to verify your email address and activate your account:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + verificationUrl + "' style='background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Verify Email Address</a>"
                + "</div>"
                + "<p style='color: #666; font-size: 14px;'>If you did not sign up for an account, you can safely ignore this email.</p>"
                + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<p style='color: #999; font-size: 12px; text-align: center;'>© 2026 CodeJudge Pro. All rights reserved.</p>"
                + "</div>";

        sendHtmlEmail(toEmail, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;
        String subject = "Reset your password - CodeJudge Pro";
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                + "<h2 style='color: #DC2626; text-align: center;'>Password Reset Request</h2>"
                + "<p>We received a request to reset your password. Click the button below to set a new password. This link will expire shortly:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + resetUrl + "' style='background-color: #DC2626; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Reset Password</a>"
                + "</div>"
                + "<p style='color: #666; font-size: 14px;'>If you did not request a password reset, please contact support immediately.</p>"
                + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<p style='color: #999; font-size: 12px; text-align: center;'>© 2026 CodeJudge Pro. All rights reserved.</p>"
                + "</div>";

        sendHtmlEmail(toEmail, subject, content);
    }

    @Override
    public void sendExamInvitationEmail(String toEmail, String examTitle, String startTime, String duration) {
        String subject = "Invitation: " + examTitle + " - CodeJudge Pro";
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>"
                + "<h2 style='color: #10B981; text-align: center;'>Exam Invitation</h2>"
                + "<p>You have been invited to participate in the upcoming assessment:</p>"
                + "<div style='background-color: #F3F4F6; padding: 15px; border-radius: 6px; margin: 20px 0;'>"
                + "<p style='margin: 5px 0;'><strong>Assessment:</strong> " + examTitle + "</p>"
                + "<p style='margin: 5px 0;'><strong>Starts At:</strong> " + startTime + "</p>"
                + "<p style='margin: 5px 0;'><strong>Duration:</strong> " + duration + " minutes</p>"
                + "</div>"
                + "<p>Please sign in to the CodeJudge Pro platform at the scheduled start time to begin your assessment.</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + frontendUrl + "/dashboard' style='background-color: #10B981; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Go to Dashboard</a>"
                + "</div>"
                + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<p style='color: #999; font-size: 12px; text-align: center;'>© 2026 CodeJudge Pro. All rights reserved.</p>"
                + "</div>";

        sendHtmlEmail(toEmail, subject, content);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully sent email to {} with subject: {}", toEmail, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}, error: {}", toEmail, e.getMessage());
            // In development, we do not want to block execution if email server is unreachable,
            // but we want to log the error. In production this would be handled properly.
        }
    }
}
