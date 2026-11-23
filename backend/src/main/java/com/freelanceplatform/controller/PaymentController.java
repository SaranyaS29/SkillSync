package com.freelanceplatform.controller;

import com.freelanceplatform.dto.payment.*;
import com.freelanceplatform.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api") @Tag(name="Payments")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service) { this.service=service; }
    @GetMapping("/payments/my") @PreAuthorize("hasAnyRole('CLIENT','FREELANCER')") @Operation(summary="List payment status visible to your account")
    public List<PaymentResponse> mine(Authentication auth) { return service.mine(auth.getName()); }
    @GetMapping("/projects/{projectId}/payments") @PreAuthorize("hasAnyRole('CLIENT','FREELANCER')") @Operation(summary="List project payment history")
    public List<PaymentResponse> project(@PathVariable Long projectId, Authentication auth) { return service.forProject(projectId, auth.getName()); }
    @PostMapping("/payments/{id}/checkout") @PreAuthorize("hasRole('CLIENT')") @Operation(summary="Start sandbox checkout; this does not mark a payment as paid")
    public PaymentResponse checkout(@PathVariable Long id, Authentication auth) { return service.checkout(id, auth.getName()); }
    @GetMapping(value="/payments/{id}/receipt",produces=MediaType.TEXT_PLAIN_VALUE) @PreAuthorize("hasAnyRole('CLIENT','FREELANCER')")
    public ResponseEntity<String> receipt(@PathVariable Long id,Authentication auth){String text=service.receipt(id,auth.getName());return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("receipt-"+id+".txt").build().toString()).contentType(MediaType.TEXT_PLAIN).body(text);}
    @PostMapping("/payments/webhook/sandbox") @ResponseStatus(HttpStatus.OK) @Operation(summary="Process a sandbox event with a valid HMAC signature")
    public PaymentResponse webhook(@Valid @RequestBody SandboxPaymentEvent event) { return service.handleSandboxEvent(event); }
}
