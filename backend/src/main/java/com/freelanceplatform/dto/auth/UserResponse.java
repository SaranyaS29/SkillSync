package com.freelanceplatform.dto.auth;

import java.util.Set;

public record UserResponse(Long id, String fullName, String email, String status, Set<String> roles) {}
