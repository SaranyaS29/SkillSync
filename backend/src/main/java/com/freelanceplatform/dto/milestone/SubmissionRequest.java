package com.freelanceplatform.dto.milestone;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record SubmissionRequest(@NotBlank @Size(max=12000) String description, @Size(max=4000) String comments) {}
