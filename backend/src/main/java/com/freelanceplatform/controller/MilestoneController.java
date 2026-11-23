package com.freelanceplatform.controller;

import com.freelanceplatform.dto.milestone.*;
import com.freelanceplatform.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @Tag(name="Milestones")
public class MilestoneController {
    private final MilestoneService service;
    public MilestoneController(MilestoneService service) { this.service=service; }
    @PostMapping("/api/assignments/{assignmentId}/milestones") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('CLIENT')") @Operation(summary="Add a milestone to an assignment")
    public MilestoneResponse create(@PathVariable Long assignmentId, @Valid @RequestBody MilestoneRequest request, Authentication auth) { return service.create(assignmentId, auth.getName(), request); }
    @GetMapping("/api/assignments/{assignmentId}/milestones") @PreAuthorize("hasAnyRole('CLIENT','FREELANCER')") @Operation(summary="List assignment milestones")
    public List<MilestoneResponse> list(@PathVariable Long assignmentId, Authentication auth) { return service.list(assignmentId, auth.getName()); }
    @PutMapping("/api/milestones/{id}/start") @PreAuthorize("hasRole('FREELANCER')") @Operation(summary="Start work on an assigned milestone")
    public MilestoneResponse start(@PathVariable Long id, Authentication auth) { return service.start(id, auth.getName()); }
    @PostMapping("/api/milestones/{id}/progress") @PreAuthorize("hasRole('FREELANCER')") @Operation(summary="Post a progress update for an assigned milestone")
    public MilestoneResponse progress(@PathVariable Long id, @Valid @RequestBody ProgressUpdateRequest request, Authentication auth) { return service.updateProgress(id, auth.getName(), request); }
    @PostMapping("/api/milestones/{id}/submit") @PreAuthorize("hasRole('FREELANCER')") @Operation(summary="Submit milestone work for review")
    public MilestoneResponse submit(@PathVariable Long id, @Valid @RequestBody SubmissionRequest request, Authentication auth) { return service.submit(id, auth.getName(), request); }
    @PutMapping("/api/milestones/{id}/approve") @PreAuthorize("hasRole('CLIENT')") @Operation(summary="Approve submitted work and create a pending payment record")
    public MilestoneResponse approve(@PathVariable Long id, Authentication auth) { return service.approve(id, auth.getName()); }
    @PutMapping("/api/milestones/{id}/request-revision") @PreAuthorize("hasRole('CLIENT')") @Operation(summary="Request a revision with feedback")
    public MilestoneResponse revise(@PathVariable Long id, @Valid @RequestBody RevisionRequest request, Authentication auth) { return service.requestRevision(id, auth.getName(), request); }
}
