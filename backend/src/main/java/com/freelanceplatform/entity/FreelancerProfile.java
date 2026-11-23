package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "freelancer_profiles")
@Getter @Setter @NoArgsConstructor
public class FreelancerProfile {
    @Id @Column(name = "user_id") private Long userId;
    @MapsId @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private UserAccount user;
    @Column(length = 160) private String headline;
    @Column(columnDefinition = "text") private String description;
    @Column(name = "hourly_rate", precision = 12, scale = 2) private BigDecimal hourlyRate;
    @Column(name = "experience_years") private Integer experienceYears;
    @Column(nullable = false, length = 24) private String availability = "AVAILABLE";
    @Column(length = 120) private String location;
    @Column(name = "profile_image_key", length = 500) private String profileImageKey;
    @Column(name = "resume_key", length = 500) private String resumeKey;
    @ManyToMany
    @JoinTable(name="freelancer_skills", joinColumns=@JoinColumn(name="freelancer_id"), inverseJoinColumns=@JoinColumn(name="skill_id"))
    private Set<Skill> skills = new HashSet<>();
}
