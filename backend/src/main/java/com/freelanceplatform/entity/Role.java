package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 32) private String name;
}
