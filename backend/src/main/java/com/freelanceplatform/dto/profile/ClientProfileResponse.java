package com.freelanceplatform.dto.profile;

public record ClientProfileResponse(Long userId, String fullName, String email, String companyName,
                                    String companyDescription, String website, String location,Double averageRating,Long reviewCount) {}
