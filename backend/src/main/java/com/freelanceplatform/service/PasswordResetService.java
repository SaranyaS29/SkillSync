package com.freelanceplatform.service;

import com.freelanceplatform.entity.PasswordResetToken;
import com.freelanceplatform.repository.PasswordResetTokenRepository;
import com.freelanceplatform.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private final UserAccountRepository users; private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder; private final JavaMailSender mailSender;
    private final String from; private final String resetUrl; private final SecureRandom random = new SecureRandom();
    public PasswordResetService(UserAccountRepository users, PasswordResetTokenRepository tokens, PasswordEncoder encoder,
            JavaMailSender mailSender, @Value("${app.mail.from}") String from, @Value("${app.mail.reset-url}") String resetUrl) {
        this.users=users; this.tokens=tokens; this.encoder=encoder; this.mailSender=mailSender; this.from=from; this.resetUrl=resetUrl;
    }
    @Transactional
    public void request(String email) {
        var user = users.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null) return;
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.deleteByUserIdAndUsedAtIsNull(user.getId());
        PasswordResetToken token = new PasswordResetToken(); token.setUser(user); token.setTokenHash(hash(raw)); token.setExpiresAt(Instant.now().plusSeconds(1800)); tokens.save(token);
        SimpleMailMessage message = new SimpleMailMessage(); message.setFrom(from); message.setTo(user.getEmail());
        message.setSubject("Reset your SkillSync password"); message.setText("Use this one-time link within 30 minutes to reset your password:\n\n" + resetUrl + "?token=" + raw + "\n\nIf you did not request this, ignore this email.");
        try { mailSender.send(message); } catch (MailException exception) { log.warn("Password reset email delivery failed"); }
    }
    @Transactional
    public void reset(String rawToken, String newPassword) {
        var token = tokens.findByTokenHashAndUsedAtIsNull(hash(rawToken)).orElseThrow(() -> new IllegalArgumentException("Reset token is invalid or expired"));
        if (!token.getExpiresAt().isAfter(Instant.now())) throw new IllegalArgumentException("Reset token is invalid or expired");
        var user = token.getUser(); user.setPasswordHash(encoder.encode(newPassword)); token.setUsedAt(Instant.now());
        tokens.deleteByUserIdAndUsedAtIsNull(user.getId());
    }
    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception exception) { throw new IllegalStateException("Could not hash reset token", exception); }
    }
}
