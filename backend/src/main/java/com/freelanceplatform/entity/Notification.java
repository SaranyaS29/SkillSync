package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="notifications", indexes=@Index(name="idx_notifications_user_read_created", columnList="user_id,read_at,created_at"))
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id", nullable=false) private UserAccount user;
    @Column(nullable=false, length=48) private String type;
    @Column(name="resource_type", length=48) private String resourceType;
    @Column(name="resource_id") private Long resourceId;
    @Column(nullable=false, columnDefinition="text") private String message;
    @Column(name="read_at") private Instant readAt;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
