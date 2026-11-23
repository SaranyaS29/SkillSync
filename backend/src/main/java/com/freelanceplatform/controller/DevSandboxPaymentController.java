package com.freelanceplatform.controller;

import com.freelanceplatform.dto.payment.PaymentResponse;
import com.freelanceplatform.service.PaymentService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/dev/payments") @Profile("dev")
public class DevSandboxPaymentController {
    private final PaymentService service;
    public DevSandboxPaymentController(PaymentService service){this.service=service;}
    @PostMapping("/{id}/simulate-success") @PreAuthorize("hasRole('CLIENT')")
    public PaymentResponse simulate(@PathVariable Long id,Authentication auth){return service.simulateSuccess(id,auth.getName());}
}
