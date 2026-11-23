package com.freelanceplatform.dto.milestone;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MilestoneRequest(@NotBlank @Size(max=160) String title, @Size(max=12000) String description,
        @NotNull @FutureOrPresent LocalDate dueDate, @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal amount,
        @NotNull @Min(1) Integer sequenceNumber) {}
