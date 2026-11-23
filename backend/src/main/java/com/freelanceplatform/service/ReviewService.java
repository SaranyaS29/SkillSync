package com.freelanceplatform.service;

import com.freelanceplatform.dto.review.*;
import com.freelanceplatform.entity.*;
import com.freelanceplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviews; private final AssignmentRepository assignments; private final UserAccountRepository users; private final NotificationService notifications;
    public ReviewService(ReviewRepository reviews,AssignmentRepository assignments,UserAccountRepository users,NotificationService notifications) { this.reviews=reviews; this.assignments=assignments; this.users=users; this.notifications=notifications; }
    @Transactional
    public ReviewResponse create(Long assignmentId,String email,ReviewRequest request) {
        Assignment assignment=assignments.findById(assignmentId).orElseThrow(()->new IllegalArgumentException("Assignment not found"));
        UserAccount reviewer=users.findByEmailIgnoreCase(email).orElseThrow();
        if(assignment.getStatus()!=Assignment.AssignmentStatus.COMPLETED) throw new IllegalArgumentException("Reviews are allowed after project completion");
        UserAccount reviewee;
        if(assignment.getClient().getId().equals(reviewer.getId())) reviewee=assignment.getFreelancer();
        else if(assignment.getFreelancer().getId().equals(reviewer.getId())) reviewee=assignment.getClient();
        else throw new IllegalArgumentException("Assignment not found");
        if(reviews.existsByAssignmentIdAndReviewerId(assignmentId,reviewer.getId())) throw new IllegalArgumentException("You already reviewed this project");
        Review review=new Review(); review.setAssignment(assignment); review.setReviewer(reviewer); review.setReviewee(reviewee); review.setRating(request.rating()); review.setFeedback(request.feedback().trim());
        review=reviews.save(review);
        notifications.notify(reviewee,"NEW_REVIEW","ASSIGNMENT",assignmentId,"You received a project review");
        return response(review);
    }
    @Transactional(readOnly=true)
    public ReviewSummary byUser(Long userId) {
        if(!users.existsById(userId)) throw new IllegalArgumentException("User not found");
        List<ReviewResponse> results=reviews.findByRevieweeIdOrderByCreatedAtDesc(userId).stream().map(this::response).toList();
        Double average=reviews.averageForUser(userId);
        return new ReviewSummary(userId,average, (long)results.size(),results);
    }
    @Transactional(readOnly=true)
    public List<ReviewResponse> mine(String email) {
        var user=users.findByEmailIgnoreCase(email).orElseThrow();
        return reviews.findByRevieweeIdOrderByCreatedAtDesc(user.getId()).stream().map(this::response).toList();
    }
    @Transactional(readOnly=true)
    public List<ReviewResponse> written(String email){var user=users.findByEmailIgnoreCase(email).orElseThrow();return reviews.findByReviewerIdOrderByCreatedAtDesc(user.getId()).stream().map(this::response).toList();}
    private ReviewResponse response(Review r) { return new ReviewResponse(r.getId(),r.getAssignment().getId(),r.getReviewer().getId(),r.getReviewer().getFullName(),r.getReviewee().getId(),r.getReviewee().getFullName(),r.getRating(),r.getFeedback(),r.getCreatedAt()); }
}
