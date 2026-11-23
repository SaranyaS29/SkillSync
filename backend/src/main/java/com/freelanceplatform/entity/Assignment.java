package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="assignments")
@Getter @Setter @NoArgsConstructor
public class Assignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="project_id", nullable=false, unique=true) private Project project;
    @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="proposal_id", nullable=false, unique=true) private Proposal proposal;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="client_id", nullable=false) private UserAccount client;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="freelancer_id", nullable=false) private UserAccount freelancer;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private AssignmentStatus status = AssignmentStatus.ACTIVE;
    @Column(name="started_at", nullable=false) private Instant startedAt;
    @Column(name="completed_at") private Instant completedAt;
    @PrePersist void onCreate() { if (startedAt == null) startedAt = Instant.now(); }
    public enum AssignmentStatus { ACTIVE, COMPLETED, CANCELLED }
}
