package com.freelanceplatform.dto.proposal;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProposalRequest(
        @NotBlank @Size(max=12000) String coverLetter,
        @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal proposedBudget,
        @NotNull @Min(1) @Max(3650) Integer completionDays,
        @Size(max=10) List<@NotBlank @Size(max=2048) String> portfolioLinks) {}
