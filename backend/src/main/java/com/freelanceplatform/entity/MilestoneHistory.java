package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="milestone_history", indexes=@Index(name="idx_milestone_history", columnList="milestone_id,changed_at"))
@Getter @Setter @NoArgsConstructor
public class MilestoneHistory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="milestone_id", nullable=false) private Milestone milestone;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="changed_by", nullable=false) private UserAccount changedBy;
    @Column(name="from_status", length=24) private String fromStatus;
    @Column(name="to_status", nullable=false, length=24) private String toStatus;
    @Column(columnDefinition="text") private String comment;
    @Column(name="changed_at", nullable=false, updatable=false) private Instant changedAt;
    @PrePersist void onCreate() { changedAt = Instant.now(); }
}
