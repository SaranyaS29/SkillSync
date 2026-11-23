package com.freelanceplatform.dto.profile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
public record PortfolioItemRequest(@NotBlank @Size(max=160) String title,@Size(max=4000) String description,@Pattern(regexp="^$|^https?://.+") @Size(max=2048) String url) {}
