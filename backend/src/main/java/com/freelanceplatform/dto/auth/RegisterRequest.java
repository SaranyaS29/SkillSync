package com.freelanceplatform.dto.auth;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 10, max = 72) String password,
        @NotNull AccountRole role) {
    public enum AccountRole { CLIENT, FREELANCER }
}
