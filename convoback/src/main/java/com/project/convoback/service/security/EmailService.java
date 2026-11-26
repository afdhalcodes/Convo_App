package com.project.convoback.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String recipientEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("Convo - Email Verification");

        message.setText(
                "Hello,\n\n" +
                        "Thank you for registering with Convo!\n\n" +
                        "Your email verification code is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "Please do not share this code with anyone.\n\n" +
                        "If you did not request this code, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "Team Convo"
        );

        mailSender.send(message);
    }
}
