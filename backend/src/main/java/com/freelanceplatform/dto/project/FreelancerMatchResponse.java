package com.freelanceplatform.dto.project;

import java.math.BigDecimal;
import java.util.List;

public record FreelancerMatchResponse(Long userId, String fullName, String headline, BigDecimal hourlyRate,
        Integer experienceYears, List<String> skills, int score, int skillScore, int experienceScore,
        int budgetScore, String explanation) {}
