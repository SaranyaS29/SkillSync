package com.freelanceplatform.dto.review;
public record ReviewSummary(Long userId,Double averageRating,Long reviewCount,java.util.List<ReviewResponse> reviews) {}
