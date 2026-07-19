package com.ajith.codejudge.notification.service.interfaces;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String token);

    void sendPasswordResetEmail(String toEmail, String token);

    void sendExamInvitationEmail(String toEmail, String examTitle, String startTime, String duration);
}
