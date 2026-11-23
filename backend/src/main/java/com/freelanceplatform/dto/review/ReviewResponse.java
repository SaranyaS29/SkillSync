package com.freelanceplatform.dto.review;
import java.time.Instant;
public record ReviewResponse(Long id,Long assignmentId,Long reviewerId,String reviewerName,Long revieweeId,String revieweeName,Integer rating,String feedback,Instant createdAt) {}
