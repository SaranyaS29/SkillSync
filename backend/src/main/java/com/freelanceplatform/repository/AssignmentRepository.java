package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByClientId(Long clientId);
    List<Assignment> findByFreelancerId(Long freelancerId);
    java.util.Optional<Assignment> findByProjectId(Long projectId);
}
