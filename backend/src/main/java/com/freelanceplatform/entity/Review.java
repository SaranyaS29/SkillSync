package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="reviews", uniqueConstraints=@UniqueConstraint(name="uk_review_assignment_reviewer", columnNames={"assignment_id","reviewer_id"}))
@Getter @Setter @NoArgsConstructor
public class Review {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="assignment_id", nullable=false) private Assignment assignment;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="reviewer_id", nullable=false) private UserAccount reviewer;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="reviewee_id", nullable=false) private UserAccount reviewee;
    @Column(nullable=false) private Integer rating;
    @Column(columnDefinition="text") private String feedback;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
