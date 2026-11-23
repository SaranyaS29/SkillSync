package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="proposals", indexes={@Index(name="idx_proposals_project_status", columnList="project_id,status"), @Index(name="idx_proposals_freelancer_status", columnList="freelancer_id,status")})
@Getter @Setter @NoArgsConstructor
public class Proposal {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="project_id", nullable=false) private Project project;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="freelancer_id", nullable=false) private UserAccount freelancer;
    @Column(name="cover_letter", nullable=false, columnDefinition="text") private String coverLetter;
    @Column(name="proposed_budget", nullable=false, precision=12, scale=2) private BigDecimal proposedBudget;
    @Column(name="completion_days", nullable=false) private Integer completionDays;
    @ElementCollection
    @CollectionTable(name="proposal_portfolio_links", joinColumns=@JoinColumn(name="proposal_id"))
    @Column(name="url", nullable=false, length=2048)
    private List<String> portfolioLinks = new ArrayList<>();
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private ProposalStatus status = ProposalStatus.PENDING;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public enum ProposalStatus { PENDING, ACCEPTED, REJECTED, WITHDRAWN }
}
