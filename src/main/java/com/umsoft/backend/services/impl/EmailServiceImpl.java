package com.umsoft.backend.services.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.umsoft.backend.entities.Email;
import com.umsoft.backend.entities.Request;
import com.umsoft.backend.repositories.EmailRepository;
import com.umsoft.backend.services.EmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private EmailRepository emailRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendRequestEmail(Request request, String category, String summary, String assignedEmail) {
        try {
            // Создаем email для отдела
            Email emailLog = new Email();
            emailLog.setRecipient(assignedEmail);
            emailLog.setSubject("Новая заявка: " + category);
            emailLog.setRequest(request);
            emailLog.setStatus("SENDING");
            emailLog.setCreatedAt(LocalDateTime.now());

            try {
                // Подготавливаем контекст для шаблона
                Context context = new Context();
                context.setVariable("category", category);
                context.setVariable("summary", summary);
                context.setVariable("userFullName", request.getSubmitter().getFullName());
                context.setVariable("userEmail", request.getSubmitter().getEmail());
                context.setVariable("userPhone", request.getSubmitter().getPhone() != null ? request.getSubmitter().getPhone() : "не указан");
                context.setVariable("taskDescription", request.getDescription());
                context.setVariable("requestId", request.getId());

                // Генерируем HTML из шаблона
                String htmlContent = templateEngine.process("request-email", context);

                // Создаем MimeMessage
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(assignedEmail);
                helper.setSubject("Новая заявка: " + category);
                helper.setText(htmlContent, true);

                // Отправляем
                mailSender.send(message);

                // Логируем успех
                emailLog.setStatus("SENT");
                emailLog.setSentAt(LocalDateTime.now());
                emailLog.setContent(htmlContent);

                System.out.println("\n" + "=".repeat(60));
                System.out.println("Email в отдел УСПЕШНО ОТПРАВЛЕН");
                System.out.println("Кому: " + assignedEmail);
                System.out.println("Тема: Новая заявка: " + category);
                System.out.println("=".repeat(60) + "\n");

            } catch (Exception e) {
                System.err.println("Ошибка при отправке email в отдел: " + e.getMessage());
                emailLog.setStatus("FAILED");
                emailLog.setErrorMessage(e.getMessage());
                e.printStackTrace();
            }

            // Сохраняем лог отправки в БД
            emailRepository.create(emailLog);

        } catch (Exception e) {
            System.err.println("Критическая ошибка при отправке email в отдел: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendConfirmationToClient(Request request) {
        try {
            // Создаем email для клиента
            Email emailLog = new Email();
            emailLog.setRecipient(request.getSubmitter().getEmail());
            emailLog.setSubject("Ваша заявка принята - Умный Софт");
            emailLog.setRequest(request);
            emailLog.setStatus("SENDING");
            emailLog.setCreatedAt(LocalDateTime.now());

            try {
                // Подготавливаем контекст для шаблона
                Context context = new Context();
                context.setVariable("userFullName", request.getSubmitter().getFullName());
                context.setVariable("requestId", request.getId());
                context.setVariable("requestDescription", request.getDescription());

                // Генерируем HTML из шаблона
                String htmlContent = templateEngine.process("confirmation-email", context);

                // Создаем MimeMessage
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(request.getSubmitter().getEmail());
                helper.setSubject("Ваша заявка принята - Умный Софт");
                helper.setText(htmlContent, true);

                // Отправляем
                mailSender.send(message);

                // Логируем успех
                emailLog.setStatus("SENT");
                emailLog.setSentAt(LocalDateTime.now());
                emailLog.setContent(htmlContent);

                System.out.println("\n" + "=".repeat(60));
                System.out.println("Подтверждение клиенту УСПЕШНО ОТПРАВЛЕНО");
                System.out.println("Кому: " + request.getSubmitter().getEmail());
                System.out.println("Заявка №: " + request.getId());
                System.out.println("=".repeat(60) + "\n");

            } catch (Exception e) {
                System.err.println("Ошибка при отправке подтверждения клиенту: " + e.getMessage());
                emailLog.setStatus("FAILED");
                emailLog.setErrorMessage(e.getMessage());
                e.printStackTrace();
            }

            // Сохраняем лог отправки в БД
            emailRepository.create(emailLog);

        } catch (Exception e) {
            System.err.println("Критическая ошибка при отправке подтверждения клиенту: " + e.getMessage());
            e.printStackTrace();
        }
    }
}