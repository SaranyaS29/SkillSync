package com.freelanceplatform.controller;

import com.freelanceplatform.dto.report.*;
import com.freelanceplatform.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service){this.service=service;}
    @PostMapping @PreAuthorize("hasAnyRole('CLIENT','FREELANCER')") public ReportResponse create(@Valid @RequestBody ReportRequest request,Authentication auth){return service.create(auth.getName(),request);}
}
