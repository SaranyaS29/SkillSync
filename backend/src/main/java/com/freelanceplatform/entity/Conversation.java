package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name="conversations")
@Getter @Setter @NoArgsConstructor
public class Conversation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="project_id", nullable=false) private Project project;
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="conversation_participants", joinColumns=@JoinColumn(name="conversation_id"), inverseJoinColumns=@JoinColumn(name="user_id"))
    private Set<UserAccount> participants = new HashSet<>();
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
