package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByAssignmentIdAndReviewerId(Long assignmentId, Long reviewerId);
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);
    List<Review> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId);
    long countByRevieweeId(Long revieweeId);
    @Query("select avg(r.rating) from Review r where r.reviewee.id = :userId") Double averageForUser(@Param("userId") Long userId);
}
