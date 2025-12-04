package com.postkar.project3dmodel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@postkar.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send OTP to email address
     */
    public boolean sendOtp(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Your OTP Code - PostKAR");

            String htmlContent = buildOtpEmailTemplate(otp);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", email);
            return true;

        } catch (MessagingException e) {
            logger.error("Error sending OTP email to: {}", email, e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending OTP email to: {}", email, e);
            return false;
        }
    }

    /**
     * Send welcome email after successful registration
     */
    public boolean sendWelcomeEmail(String email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Welcome to PostKAR!");

            String htmlContent = buildWelcomeEmailTemplate();
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Welcome email sent successfully to: {}", email);
            return true;

        } catch (MessagingException e) {
            logger.error("Error sending welcome email to: {}", email, e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending welcome email to: {}", email, e);
            return false;
        }
    }

    /**
     * Build OTP email HTML template
     */
    private String buildOtpEmailTemplate(String otp) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }" +
                ".content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }" +
                ".otp-box { background-color: #fff; border: 2px solid #4CAF50; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; margin: 20px 0; border-radius: 5px; }"
                +
                ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>PostKAR Verification</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Your OTP Code</h2>" +
                "<p>Use the following One-Time Password (OTP) to complete your verification:</p>" +
                "<div class='otp-box'>" + otp + "</div>" +
                "<p><strong>Important:</strong></p>" +
                "<ul>" +
                "<li>This OTP is valid for 10 minutes</li>" +
                "<li>Do not share this code with anyone</li>" +
                "<li>If you didn't request this code, please ignore this email</li>" +
                "</ul>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2025 PostKAR. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build welcome email HTML template
     */
    private String buildWelcomeEmailTemplate() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }" +
                ".content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; margin-top: 20px; }" +
                ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Welcome to PostKAR!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Registration Successful</h2>" +
                "<p>Thank you for joining PostKAR! Your account has been successfully created.</p>" +
                "<p>You can now explore all the features and start your journey with us.</p>" +
                "<p>If you have any questions or need assistance, feel free to contact our support team.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2025 PostKAR. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
