package com.freelanceplatform.repository;
import com.freelanceplatform.entity.MilestoneSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MilestoneSubmissionRepository extends JpaRepository<MilestoneSubmission, Long> {
    List<MilestoneSubmission> findByMilestoneIdOrderBySubmittedAtDesc(Long milestoneId);
}
