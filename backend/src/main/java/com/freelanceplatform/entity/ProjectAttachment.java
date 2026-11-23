package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="project_attachments")
@Getter @Setter @NoArgsConstructor
public class ProjectAttachment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="project_id", nullable=false) private Project project;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="uploaded_by", nullable=false) private UserAccount uploadedBy;
    @Column(name="storage_key", nullable=false, length=500) private String storageKey;
    @Column(name="original_name", nullable=false, length=255) private String originalName;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
