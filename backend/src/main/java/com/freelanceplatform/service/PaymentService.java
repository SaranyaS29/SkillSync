package com.freelanceplatform.service;

import com.freelanceplatform.dto.payment.PaymentResponse;
import com.freelanceplatform.dto.payment.SandboxPaymentEvent;
import com.freelanceplatform.entity.Payment;
import com.freelanceplatform.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository payments; private final AssignmentRepository assignments; private final UserAccountRepository users; private final String webhookSecret;
    private final NotificationService notifications;
    public PaymentService(PaymentRepository payments, AssignmentRepository assignments, UserAccountRepository users,
                          NotificationService notifications, @Value("${app.payments.sandbox-webhook-secret}") String webhookSecret) {
        this.payments=payments; this.assignments=assignments; this.users=users; this.notifications=notifications; this.webhookSecret=webhookSecret;
        if (webhookSecret.length() < 32) throw new IllegalArgumentException("Sandbox webhook secret must contain at least 32 characters");
    }
    @Transactional(readOnly=true)
    public List<PaymentResponse> mine(String email) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        return payments.findByAssignmentClientIdOrAssignmentFreelancerId(user.getId(), user.getId()).stream().map(this::response).toList();
    }
    @Transactional(readOnly=true)
    public List<PaymentResponse> forProject(Long projectId, String email) {
        var assignment = assignments.findByProjectId(projectId).orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        if (!assignment.getClient().getEmail().equalsIgnoreCase(email) && !assignment.getFreelancer().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Project not found");
        return payments.findByAssignmentId(assignment.getId()).stream().map(this::response).toList();
    }
    @Transactional
    public PaymentResponse checkout(Long paymentId, String email) {
        Payment payment = payments.findById(paymentId).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (!payment.getAssignment().getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Payment not found");
        if (payment.getStatus() == Payment.PaymentStatus.PAID) throw new IllegalArgumentException("Payment is already paid");
        if (payment.getStatus() == Payment.PaymentStatus.PROCESSING) return response(payment);
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment.setTransactionReference("sandbox_" + UUID.randomUUID());
        return response(payment);
    }
    @Transactional
    public PaymentResponse handleSandboxEvent(SandboxPaymentEvent event) {
        String payload = event.transactionReference() + "|" + event.successful();
        if (!validSignature(payload, event.signature())) throw new IllegalArgumentException("Invalid payment event signature");
        Payment payment = payments.findByTransactionReference(event.transactionReference()).orElseThrow(() -> new IllegalArgumentException("Payment reference not found"));
        if (payment.getStatus() == Payment.PaymentStatus.PAID) return response(payment);
        if (payment.getStatus() != Payment.PaymentStatus.PROCESSING) throw new IllegalArgumentException("Payment is not awaiting confirmation");
        if (event.successful()) { payment.setStatus(Payment.PaymentStatus.PAID); payment.setPaidAt(Instant.now()); }
        else payment.setStatus(Payment.PaymentStatus.FAILED);
        notifications.notify(payment.getAssignment().getClient(),"PAYMENT_STATUS","PAYMENT",payment.getId(),"Sandbox payment is "+payment.getStatus().name().toLowerCase());
        notifications.notify(payment.getAssignment().getFreelancer(),"PAYMENT_STATUS","PAYMENT",payment.getId(),"Sandbox payment is "+payment.getStatus().name().toLowerCase());
        return response(payment);
    }
    @Transactional(readOnly=true)
    public String receipt(Long paymentId,String email){
        Payment payment=payments.findById(paymentId).orElseThrow(()->new IllegalArgumentException("Payment not found"));
        if(!payment.getAssignment().getClient().getEmail().equalsIgnoreCase(email)&&!payment.getAssignment().getFreelancer().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Payment not found");
        if(payment.getStatus()!=Payment.PaymentStatus.PAID) throw new IllegalArgumentException("A receipt is available only for a paid sandbox payment");
        return "SkillSync sandbox payment receipt\nPayment: "+payment.getId()+"\nProject: "+payment.getAssignment().getProject().getTitle()+"\nAmount: "+payment.getAmount()+"\nStatus: "+payment.getStatus()+"\nReference: "+payment.getTransactionReference()+"\nPaid at: "+payment.getPaidAt()+"\n\nSIMULATED PAYMENT - NOT A REAL FINANCIAL TRANSACTION\n";
    }
    @Transactional
    public PaymentResponse simulateSuccess(Long paymentId,String email){
        Payment payment=payments.findById(paymentId).orElseThrow(()->new IllegalArgumentException("Payment not found"));
        if(!payment.getAssignment().getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Payment not found");
        if(payment.getStatus()!=Payment.PaymentStatus.PROCESSING) throw new IllegalArgumentException("Start checkout before completing the sandbox payment");
        String payload=payment.getTransactionReference()+"|true";
        return handleSandboxEvent(new SandboxPaymentEvent(payment.getTransactionReference(),true,hmac(payload)));
    }
    public String sign(String payload) { return hmac(payload); }
    private boolean validSignature(String payload, String signature) {
        try { return MessageDigest.isEqual(HexFormat.of().parseHex(hmac(payload)), HexFormat.of().parseHex(signature)); }
        catch (IllegalArgumentException exception) { return false; }
    }
    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) { throw new IllegalStateException("Could not sign payment event", exception); }
    }
    private PaymentResponse response(Payment p) { return new PaymentResponse(p.getId(), p.getAssignment().getId(), p.getAssignment().getProject().getId(), p.getMilestone() == null ? null : p.getMilestone().getId(), p.getAmount(), p.getStatus(), p.getTransactionReference(), p.getPaidAt()); }
}
