package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="milestones", uniqueConstraints=@UniqueConstraint(name="uk_milestone_assignment_sequence", columnNames={"assignment_id","sequence_number"}), indexes=@Index(name="idx_milestone_assignment_status_due", columnList="assignment_id,status,due_date"))
@Getter @Setter @NoArgsConstructor
public class Milestone {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="assignment_id", nullable=false) private Assignment assignment;
    @Column(nullable=false, length=160) private String title;
    @Column(columnDefinition="text") private String description;
    @Column(name="due_date") private LocalDate dueDate;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal amount;
    @Column(name="sequence_number", nullable=false) private Integer sequenceNumber;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private MilestoneStatus status = MilestoneStatus.PENDING;
    public enum MilestoneStatus { PENDING, IN_PROGRESS, SUBMITTED, REVISION_REQUESTED, APPROVED }
}
