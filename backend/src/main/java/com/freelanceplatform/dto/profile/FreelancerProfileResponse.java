package com.freelanceplatform.dto.profile;

import java.math.BigDecimal;
import java.util.List;

public record FreelancerProfileResponse(Long userId, String fullName, String headline,
        String description, BigDecimal hourlyRate, Integer experienceYears, String availability,
        String location, List<String> skills,Double averageRating,Long reviewCount,String photoUrl,boolean hasResume,List<PortfolioItemRequest> portfolioItems) {}
