package com.example.security.service.internal;

import com.example.security.model.PasswordResetToken;
import com.example.security.model.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class PasswordResetEmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(User user, PasswordResetToken passwordResetToken) {
        final var email = constructResetTokenEmail(user, passwordResetToken);
        mailSender.send(email);
    }

    private SimpleMailMessage constructResetTokenEmail(User user, PasswordResetToken passwordResetToken) {
        final var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        final var url = baseUrl + "/users/change-password?token=" + passwordResetToken.getToken();
        return constructEmail("Reset your password via link: " + url, user);
    }

    private SimpleMailMessage constructEmail(String body, User user) {
        final var email = new SimpleMailMessage();
        email.setSubject("Reset Password");
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom("app@example.com");
        return email;
    }

}
