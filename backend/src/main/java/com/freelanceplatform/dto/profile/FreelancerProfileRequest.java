package com.freelanceplatform.dto.profile;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;

public record FreelancerProfileRequest(
        @Size(max=160) String headline,
        @Size(max=12000) String description,
        @DecimalMin("0.00") @Digits(integer=10, fraction=2) BigDecimal hourlyRate,
        @Min(0) @Max(80) Integer experienceYears,
        @Pattern(regexp="AVAILABLE|LIMITED|UNAVAILABLE") String availability,
        @Size(max=120) String location,
        @Size(max=30) List<@NotBlank @Size(max=80) String> skills,
        @Size(max=20) List<@Valid PortfolioItemRequest> portfolioItems) {}
