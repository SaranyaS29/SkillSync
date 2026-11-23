package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByAssignmentId(Long assignmentId);
    List<Payment> findByAssignmentClientIdOrAssignmentFreelancerId(Long clientId, Long freelancerId);
    java.util.Optional<Payment> findByTransactionReference(String transactionReference);
}
