package com.freelanceplatform.controller;

import com.freelanceplatform.dto.proposal.ProposalRequest;
import com.freelanceplatform.dto.proposal.ProposalResponse;
import com.freelanceplatform.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @Tag(name="Proposals")
public class ProposalController {
    private final ProposalService service;
    public ProposalController(ProposalService service) { this.service = service; }

    @PostMapping("/api/projects/{projectId}/proposals") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary="Submit a proposal to an open project")
    public ProposalResponse submit(@PathVariable Long projectId, @Valid @RequestBody ProposalRequest request, Authentication auth) { return service.submit(projectId, auth.getName(), request); }
    @GetMapping("/api/proposals/my") @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary="List your proposals")
    public List<ProposalResponse> mine(Authentication auth) { return service.mine(auth.getName()); }
    @GetMapping("/api/projects/{projectId}/proposals") @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary="List proposals for a project you own")
    public List<ProposalResponse> forProject(@PathVariable Long projectId, Authentication auth) { return service.forProject(projectId, auth.getName()); }
    @PutMapping("/api/proposals/{id}/accept") @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary="Accept a proposal and assign its freelancer")
    public ProposalResponse accept(@PathVariable Long id, Authentication auth) { return service.decide(id, auth.getName(), true); }
    @PutMapping("/api/proposals/{id}/reject") @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary="Reject a proposal")
    public ProposalResponse reject(@PathVariable Long id, Authentication auth) { return service.decide(id, auth.getName(), false); }
    @PutMapping("/api/proposals/{id}/withdraw") @PreAuthorize("hasRole('FREELANCER')") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Withdraw a pending proposal you submitted")
    public void withdraw(@PathVariable Long id, Authentication auth) { service.withdraw(id, auth.getName()); }
}
