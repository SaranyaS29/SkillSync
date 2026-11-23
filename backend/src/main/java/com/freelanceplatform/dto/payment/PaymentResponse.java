package com.freelanceplatform.dto.payment;

import com.freelanceplatform.entity.Payment;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(Long id, Long assignmentId, Long projectId, Long milestoneId,
                              BigDecimal amount, Payment.PaymentStatus status,
                              String transactionReference, Instant paidAt) {}
