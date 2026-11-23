package com.freelanceplatform.dto.proposal;

import com.freelanceplatform.entity.Proposal;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProposalResponse(Long id, Long projectId, String projectTitle, Long freelancerId, String freelancerName,
                               String coverLetter, BigDecimal proposedBudget, Integer completionDays,
                               List<String> portfolioLinks, Proposal.ProposalStatus status, Instant createdAt) {}
