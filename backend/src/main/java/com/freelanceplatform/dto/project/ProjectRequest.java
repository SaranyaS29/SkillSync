package com.freelanceplatform.dto.project;

import com.freelanceplatform.entity.Project;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProjectRequest(
        @NotBlank @Size(max=180) String title,
        @NotBlank @Size(max=12000) String description,
        @NotNull Project.ProjectType projectType,
        @NotNull @DecimalMin("0.00") @Digits(integer=10, fraction=2) BigDecimal budgetMin,
        @NotNull @DecimalMin("0.00") @Digits(integer=10, fraction=2) BigDecimal budgetMax,
        @Size(max=24) String experienceLevel,
        @Size(max=80) String category,
        @FutureOrPresent LocalDate deadline,
        @Size(max=30) List<@NotBlank @Size(max=80) String> requiredSkills,
        boolean publish) {}
