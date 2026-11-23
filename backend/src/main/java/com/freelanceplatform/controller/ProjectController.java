package com.freelanceplatform.controller;

import com.freelanceplatform.dto.project.ProjectRequest;
import com.freelanceplatform.dto.project.ProjectResponse;
import com.freelanceplatform.dto.project.FreelancerMatchResponse;
import com.freelanceplatform.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/projects") @Tag(name="Projects")
public class ProjectController {
    private final ProjectService service;
    public ProjectController(ProjectService service) { this.service = service; }

    @GetMapping @Operation(summary="Browse open projects")
    public Page<ProjectResponse> browse(@RequestParam(required=false) String q, @RequestParam(required=false) String skill,
            @RequestParam(required=false) java.math.BigDecimal minBudget, @RequestParam(required=false) java.math.BigDecimal maxBudget,
            @RequestParam(required=false) String experienceLevel, @RequestParam(required=false) com.freelanceplatform.entity.Project.ProjectType projectType,
            @PageableDefault(size=20, sort="createdAt", direction=Sort.Direction.DESC) Pageable pageable) {
        return service.browse(q, skill, minBudget, maxBudget, experienceLevel, projectType, pageable);
    }
    @GetMapping("/mine") @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary="List projects you posted")
    public Page<ProjectResponse> mine(@PageableDefault(size=20, sort="createdAt") Pageable pageable, Authentication auth) { return service.mine(auth.getName(), pageable); }
    @GetMapping("/{id}") @Operation(summary="Get an open project or your own project")
    public ProjectResponse get(@PathVariable Long id, Authentication authentication) {
        return service.get(id, authentication == null ? "" : authentication.getName());
    }
    @GetMapping("/{id}/matches") @PreAuthorize("hasRole('CLIENT')") @Operation(summary="Find freelancers with an explainable match score")
    public java.util.List<FreelancerMatchResponse> matches(@PathVariable Long id, Authentication authentication) { return service.matches(id, authentication.getName()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary="Create a project as a client")
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request, Authentication authentication) { return service.create(authentication.getName(), request); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary="Edit a project you own")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request, Authentication authentication) { return service.update(id, authentication.getName(), request); }
    @PutMapping("/{id}/cancel") @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary="Cancel a draft or open project that has no accepted proposal")
    public ProjectResponse cancel(@PathVariable Long id, Authentication authentication) { return service.cancel(id, authentication.getName()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary="Delete a draft project you own")
    public void delete(@PathVariable Long id, Authentication authentication) { service.delete(id, authentication.getName()); }
}
