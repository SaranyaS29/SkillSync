package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="password_reset_tokens", indexes=@Index(name="idx_password_reset_user_expiry", columnList="user_id,expires_at"))
@Getter @Setter @NoArgsConstructor
public class PasswordResetToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id", nullable=false) private UserAccount user;
    @Column(name="token_hash", nullable=false, unique=true, length=64) private String tokenHash;
    @Column(name="expires_at", nullable=false) private Instant expiresAt;
    @Column(name="used_at") private Instant usedAt;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
