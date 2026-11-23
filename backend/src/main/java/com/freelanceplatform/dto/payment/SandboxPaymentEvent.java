package com.freelanceplatform.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SandboxPaymentEvent(@NotBlank String transactionReference, @NotNull Boolean successful,
                                  @NotBlank String signature) {}
