package com.freelanceplatform.dto.milestone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevisionRequest(@NotBlank @Size(max=4000) String comment) {}
