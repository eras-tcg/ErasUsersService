package com.auth.users_service.service;

import com.auth.users_service.config.VerificationProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@lombok.RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerificationProperties verificationProperties;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendVerificationEmail(String to, String token) {
        String link = verificationProperties.getBaseUrl() + "?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setFrom(fromAddress, "ERAS");
            helper.setTo(to);
            helper.setSubject("Confirma tu cuenta en ErasTCG");
            helper.setText(
                "<div style='font-family:sans-serif;max-width:480px;margin:auto'>" +
                "<h2>Bienvenido a la comunidad de ErasTCG</h2>" +
                "<p>Por favor verifica tu correo electrónico haciendo clic en el botón de abajo.</p>" +
                "<p>Este enlace permanecerá activo hasta que lo utilices.</p>" +
                "<a href='" + link + "' style='display:inline-block;padding:12px 24px;" +
                "background:#4f46e5;color:#fff;text-decoration:none;border-radius:6px;" +
                "font-weight:bold'>Verificar cuenta</a>" +
                "<p style='color:#888;font-size:12px;margin-top:24px'>" +
                "Si no fuiste tu quien registró esta cuenta, puedes ignorar este correo.</p>" +
                "</div>",
                true
            );
            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

}
