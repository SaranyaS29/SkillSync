package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="reports", indexes=@Index(name="idx_reports_status_created", columnList="status,created_at"))
@Getter @Setter @NoArgsConstructor
public class Report {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="reporter_id", nullable=false) private UserAccount reporter;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="target_user_id") private UserAccount targetUser;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_id") private Project project;
    @Column(nullable=false, length=24) private String status = "OPEN";
    @Column(nullable=false, columnDefinition="text") private String reason;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
