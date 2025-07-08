package com.elearning.elearning_backend.Service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // Send HTML email directly (for notifications with custom HTML)
    public void sendDirectHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("phamquangnamhuy1908@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email HTML", e);
        }
    }

    // Original method using Thymeleaf template (kept for backward compatibility)
    public void sendHtmlEmail(String to, String subject, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("phamquangnamhuy1908@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);

            // Inject biến vào template Thymeleaf
            Context context = new Context();
            context.setVariable("title", title);
            context.setVariable("content", content);

            String htmlContent = templateEngine.process("notification-email.html", context);
            helper.setText(htmlContent, true); // true = HTML content

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email HTML", e);
        }
    }
}
