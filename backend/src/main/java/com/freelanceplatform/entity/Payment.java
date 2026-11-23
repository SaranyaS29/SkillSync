package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="payments", indexes=@Index(name="idx_payment_assignment_status", columnList="assignment_id,status"))
@Getter @Setter @NoArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="assignment_id", nullable=false) private Assignment assignment;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="milestone_id") private Milestone milestone;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private PaymentStatus status = PaymentStatus.PENDING;
    @Column(name="transaction_reference", length=120) private String transactionReference;
    @Column(name="paid_at") private Instant paidAt;
    public enum PaymentStatus { PENDING, PROCESSING, PAID, FAILED, REFUNDED }
}
