package com.freelanceplatform.dto.assignment;

import com.freelanceplatform.entity.Assignment;
import java.math.BigDecimal;
import java.time.Instant;

public record AssignmentResponse(Long id, Long projectId, String projectTitle, Long clientId,
        String clientName, String clientCompanyName, String clientCompanyDescription, String clientWebsite, String clientLocation,
        Long freelancerId, String freelancerName, BigDecimal agreedBudget,
        Assignment.AssignmentStatus status, Instant startedAt, Instant completedAt) {}
