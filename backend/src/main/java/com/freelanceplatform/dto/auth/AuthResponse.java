package com.freelanceplatform.dto.auth;

import java.util.Set;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserSummary user) {
    public record UserSummary(Long id, String fullName, String email, Set<String> roles) {}
}
