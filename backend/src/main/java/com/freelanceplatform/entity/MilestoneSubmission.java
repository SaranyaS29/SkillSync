package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="milestone_submissions", indexes=@Index(name="idx_submission_milestone", columnList="milestone_id,submitted_at"))
@Getter @Setter @NoArgsConstructor
public class MilestoneSubmission {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="milestone_id", nullable=false) private Milestone milestone;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="submitted_by", nullable=false) private UserAccount submittedBy;
    @Column(columnDefinition="text") private String description;
    @Column(columnDefinition="text") private String comments;
    @Column(name="submitted_at", nullable=false, updatable=false) private Instant submittedAt;
    @OneToMany(mappedBy="submission", cascade=CascadeType.ALL, orphanRemoval=true) private List<SubmissionFile> files = new ArrayList<>();
    @PrePersist void onCreate() { submittedAt = Instant.now(); }
}
