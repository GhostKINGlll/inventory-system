package com.inventory.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final Map<String, CodeInfo> codeCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public EmailService(JavaMailSender mailSender, @org.springframework.beans.factory.annotation.Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationCode(String email) {
        String code = String.format("%06d", random.nextInt(1000000));
        codeCache.put(email, new CodeInfo(code, LocalDateTime.now()));

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("进销存管理系统 - 邮箱验证码");
            helper.setText("您的验证码为：<b>" + code + "</b>，5分钟内有效。", true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    public boolean verifyCode(String email, String code) {
        CodeInfo info = codeCache.get(email);
        if (info == null) return false;
        if (info.code().equals(code) && info.time().plusMinutes(5).isAfter(LocalDateTime.now())) {
            codeCache.remove(email);
            return true;
        }
        return false;
    }

    public void sendFeedback(String userEmail, String username, String subject, String message) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setReplyTo(userEmail);
            helper.setTo(fromAddress);
            helper.setSubject("用户反馈 - " + subject);
            helper.setText("""
                <h3>用户反馈</h3>
                <p><b>用户名：</b>%s</p>
                <p><b>用户邮箱：</b>%s</p>
                <p><b>主题：</b>%s</p>
                <hr>
                <p><b>反馈内容：</b></p>
                <p>%s</p>
                """.formatted(username, userEmail, subject, message), true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("反馈发送失败: " + e.getMessage());
        }
    }

    private record CodeInfo(String code, LocalDateTime time) {}
}
