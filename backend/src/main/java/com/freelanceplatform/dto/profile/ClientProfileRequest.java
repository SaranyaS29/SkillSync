package com.freelanceplatform.dto.profile;

import jakarta.validation.constraints.Size;

public record ClientProfileRequest(@Size(max=160) String companyName, @Size(max=12000) String companyDescription,
                                   @Size(max=255) String website, @Size(max=120) String location) {}
