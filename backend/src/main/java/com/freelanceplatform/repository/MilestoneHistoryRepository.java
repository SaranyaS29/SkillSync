package com.freelanceplatform.repository;
import com.freelanceplatform.entity.MilestoneHistory;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MilestoneHistoryRepository extends JpaRepository<MilestoneHistory, Long> {
    java.util.List<MilestoneHistory> findByMilestoneIdOrderByChangedAtAsc(Long milestoneId);
}
