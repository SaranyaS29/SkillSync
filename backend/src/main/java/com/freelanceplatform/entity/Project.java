package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "projects", indexes = {@Index(name="idx_projects_status_created", columnList="status,created_at"), @Index(name="idx_projects_client", columnList="client_id")})
@Getter @Setter @NoArgsConstructor
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_id", nullable = false) private UserAccount client;
    @Column(nullable = false, length = 180) private String title;
    @Column(nullable = false, columnDefinition = "text") private String description;
    @Enumerated(EnumType.STRING) @Column(name="project_type", nullable=false, length=20) private ProjectType projectType;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private ProjectStatus status = ProjectStatus.DRAFT;
    @Column(name="budget_min", nullable=false, precision=12, scale=2) private BigDecimal budgetMin;
    @Column(name="budget_max", nullable=false, precision=12, scale=2) private BigDecimal budgetMax;
    @Column(name="experience_level", length=24) private String experienceLevel;
    @Column(length=80) private String category;
    private LocalDate deadline;
    @ManyToMany
    @JoinTable(name="project_skills", joinColumns=@JoinColumn(name="project_id"), inverseJoinColumns=@JoinColumn(name="skill_id"))
    private Set<Skill> requiredSkills = new HashSet<>();
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public enum ProjectType { FIXED_PRICE, HOURLY }
    public enum ProjectStatus { DRAFT, OPEN, IN_PROGRESS, COMPLETED, CANCELLED }
}
