package com.freelanceplatform.controller;

import com.freelanceplatform.dto.profile.*;
import com.freelanceplatform.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController @Tag(name="Profiles")
public class ProfileController {
    private final ProfileService service;
    public ProfileController(ProfileService service) { this.service = service; }
    @GetMapping("/api/freelancers") @Operation(summary="Search freelancer profiles")
    public Page<FreelancerProfileResponse> search(@RequestParam(required=false) String skill,
            @RequestParam(required=false) BigDecimal minRate, @RequestParam(required=false) BigDecimal maxRate,
            @RequestParam(required=false) Integer minExperience, @RequestParam(required=false) Integer maxExperience,
            @RequestParam(required=false) String availability, @RequestParam(required=false) Double minRating,
            @PageableDefault(size=20) Pageable pageable) {
        return service.searchFreelancers(skill, minRate, maxRate, minExperience, maxExperience, availability, minRating, pageable);
    }
    @GetMapping("/api/freelancers/{id}") @Operation(summary="View a freelancer profile")
    public FreelancerProfileResponse freelancer(@PathVariable Long id) { return service.freelancer(id); }
    @PutMapping("/api/freelancers/profile") @PreAuthorize("hasRole('FREELANCER')") @Operation(summary="Update your freelancer profile")
    public FreelancerProfileResponse updateFreelancer(@Valid @RequestBody FreelancerProfileRequest request, Authentication auth) { return service.updateFreelancer(auth.getName(), request); }
    @GetMapping("/api/clients/profile") @PreAuthorize("hasRole('CLIENT')") @Operation(summary="Get your client profile")
    public ClientProfileResponse client(Authentication auth) { return service.client(auth.getName()); }
    @PutMapping("/api/clients/profile") @PreAuthorize("hasRole('CLIENT')") @Operation(summary="Update your client profile")
    public ClientProfileResponse updateClient(@Valid @RequestBody ClientProfileRequest request, Authentication auth) { return service.updateClient(auth.getName(), request); }
}
