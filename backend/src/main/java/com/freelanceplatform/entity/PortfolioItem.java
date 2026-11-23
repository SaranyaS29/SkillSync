package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="portfolio_items",indexes=@Index(name="idx_portfolio_freelancer",columnList="freelancer_id"))
@Getter @Setter @NoArgsConstructor
public class PortfolioItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="freelancer_id",nullable=false) private UserAccount freelancer;
    @Column(nullable=false,length=160) private String title;
    @Column(columnDefinition="text") private String description;
    @Column(length=2048) private String url;
}
