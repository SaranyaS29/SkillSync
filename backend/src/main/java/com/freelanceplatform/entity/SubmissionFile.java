package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="submission_files")
@Getter @Setter @NoArgsConstructor
public class SubmissionFile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="submission_id", nullable=false) private MilestoneSubmission submission;
    @Column(name="storage_key", nullable=false, length=500) private String storageKey;
    @Column(name="original_name", nullable=false, length=255) private String originalName;
}
