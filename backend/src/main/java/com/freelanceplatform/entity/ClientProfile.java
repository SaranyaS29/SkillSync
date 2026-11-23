package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "client_profiles")
@Getter @Setter @NoArgsConstructor
public class ClientProfile {
    @Id @Column(name = "user_id") private Long userId;
    @MapsId @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private UserAccount user;
    @Column(name = "company_name", length = 160) private String companyName;
    @Column(name = "company_description", columnDefinition = "text") private String companyDescription;
    @Column(length = 255) private String website;
    @Column(length = 120) private String location;
}
