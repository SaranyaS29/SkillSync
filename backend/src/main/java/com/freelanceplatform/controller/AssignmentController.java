package com.freelanceplatform.controller;

import com.freelanceplatform.dto.assignment.AssignmentResponse;
import com.freelanceplatform.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/assignments") @Tag(name="Assignments")
public class AssignmentController {
    private final AssignmentService service;
    public AssignmentController(AssignmentService service) { this.service=service; }
    @GetMapping("/my") @PreAuthorize("hasAnyRole('CLIENT','FREELANCER')") @Operation(summary="List your project assignments")
    public List<AssignmentResponse> mine(Authentication auth) {
        boolean isClient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        return service.mine(auth.getName(), isClient);
    }
}
