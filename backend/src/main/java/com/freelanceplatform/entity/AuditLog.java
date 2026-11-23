package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="audit_logs", indexes=@Index(name="idx_audit_created", columnList="created_at"))
@Getter @Setter @NoArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="actor_id") private UserAccount actor;
    @Column(nullable=false, length=80) private String action;
    @Column(name="resource_type", nullable=false, length=48) private String resourceType;
    @Column(name="resource_id") private Long resourceId;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
