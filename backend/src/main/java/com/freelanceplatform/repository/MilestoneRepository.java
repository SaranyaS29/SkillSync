package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByAssignmentIdOrderBySequenceNumber(Long assignmentId);
    List<Milestone> findByDueDateAndStatusIn(java.time.LocalDate dueDate, java.util.List<Milestone.MilestoneStatus> statuses);
    @Query("select sum(m.amount) from Milestone m where m.assignment.id = :assignmentId")
    BigDecimal sumAmountByAssignmentId(@Param("assignmentId") Long assignmentId);
}
