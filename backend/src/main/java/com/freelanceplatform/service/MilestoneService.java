package com.freelanceplatform.service;

import com.freelanceplatform.dto.milestone.*;
import com.freelanceplatform.entity.*;
import com.freelanceplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class MilestoneService {
    private final AssignmentRepository assignments; private final MilestoneRepository milestones;
    private final MilestoneHistoryRepository history; private final MilestoneSubmissionRepository submissions;
    private final PaymentRepository payments; private final UserAccountRepository users;
    private final NotificationService notifications;
    private final SubmissionFileRepository submissionFiles;
    private final AuditLogService audit;
    public MilestoneService(AssignmentRepository assignments, MilestoneRepository milestones, MilestoneHistoryRepository history,
                            MilestoneSubmissionRepository submissions, PaymentRepository payments, UserAccountRepository users, NotificationService notifications,SubmissionFileRepository submissionFiles,AuditLogService audit) {
        this.assignments=assignments; this.milestones=milestones; this.history=history; this.submissions=submissions; this.payments=payments; this.users=users; this.notifications=notifications;this.submissionFiles=submissionFiles;this.audit=audit;
    }
    @Transactional
    public MilestoneResponse create(Long assignmentId, String email, MilestoneRequest request) {
        Assignment assignment = assignments.findById(assignmentId).orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        if (!assignment.getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Assignment not found");
        if (assignment.getStatus() != Assignment.AssignmentStatus.ACTIVE) throw new IllegalArgumentException("Assignment is not active");
        BigDecimal planned = milestones.sumAmountByAssignmentId(assignmentId);
        if (planned == null) planned = BigDecimal.ZERO;
        if (planned.add(request.amount()).compareTo(assignment.getProposal().getProposedBudget()) > 0)
            throw new IllegalArgumentException("Milestone totals cannot exceed the accepted proposal budget");
        Milestone milestone = new Milestone(); milestone.setAssignment(assignment); milestone.setTitle(request.title().trim());
        milestone.setDescription(request.description()); milestone.setDueDate(request.dueDate()); milestone.setAmount(request.amount());
        milestone.setSequenceNumber(request.sequenceNumber()); milestone.setStatus(Milestone.MilestoneStatus.PENDING);
        milestone = milestones.save(milestone); record(milestone, null, Milestone.MilestoneStatus.PENDING, email, "Milestone created");
        return response(milestone);
    }
    @Transactional(readOnly=true)
    public List<MilestoneResponse> list(Long assignmentId, String email) {
        Assignment assignment = assignments.findById(assignmentId).orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        ensureParticipant(assignment, email);
        return milestones.findByAssignmentIdOrderBySequenceNumber(assignmentId).stream().map(this::response).toList();
    }
    @Transactional
    public MilestoneResponse start(Long id, String email) {
        Milestone milestone = milestones.findById(id).orElseThrow(() -> new IllegalArgumentException("Milestone not found"));
        ensureAssignedFreelancer(milestone, email);
        if (milestone.getAssignment().getStatus() != Assignment.AssignmentStatus.ACTIVE)
            throw new IllegalArgumentException("This assignment is not active");
        if (milestone.getStatus() != Milestone.MilestoneStatus.PENDING)
            throw new IllegalArgumentException("Only pending milestones can be started");
        milestone.setStatus(Milestone.MilestoneStatus.IN_PROGRESS);
        record(milestone, Milestone.MilestoneStatus.PENDING, milestone.getStatus(), email, "Freelancer started work");
        notifications.notify(milestone.getAssignment().getClient(), "MILESTONE_STARTED", "MILESTONE", milestone.getId(), "Work started on milestone: " + milestone.getTitle());
        audit.record(email, "MILESTONE_STARTED", "MILESTONE", milestone.getId());
        return response(milestone);
    }
    @Transactional
    public MilestoneResponse updateProgress(Long id, String email, ProgressUpdateRequest request) {
        Milestone milestone = milestones.findById(id).orElseThrow(() -> new IllegalArgumentException("Milestone not found"));
        ensureAssignedFreelancer(milestone, email);
        if (milestone.getAssignment().getStatus() != Assignment.AssignmentStatus.ACTIVE)
            throw new IllegalArgumentException("This assignment is not active");
        if (milestone.getStatus() != Milestone.MilestoneStatus.PENDING && milestone.getStatus() != Milestone.MilestoneStatus.IN_PROGRESS && milestone.getStatus() != Milestone.MilestoneStatus.REVISION_REQUESTED)
            throw new IllegalArgumentException("Progress can only be updated on work in progress");
        var before = milestone.getStatus();
        if (before == Milestone.MilestoneStatus.PENDING) milestone.setStatus(Milestone.MilestoneStatus.IN_PROGRESS);
        record(milestone, before, milestone.getStatus(), email, request.comment().trim());
        notifications.notify(milestone.getAssignment().getClient(), "MILESTONE_PROGRESS", "MILESTONE", milestone.getId(), "Progress update for milestone '" + milestone.getTitle() + "': " + request.comment().trim());
        audit.record(email, "MILESTONE_PROGRESS_UPDATED", "MILESTONE", milestone.getId());
        return response(milestone);
    }
    @Transactional
    public MilestoneResponse submit(Long id, String email, SubmissionRequest request) {
        Milestone milestone = milestones.findById(id).orElseThrow(() -> new IllegalArgumentException("Milestone not found"));
        ensureAssignedFreelancer(milestone, email);
        if (milestone.getAssignment().getStatus() != Assignment.AssignmentStatus.ACTIVE)
            throw new IllegalArgumentException("This assignment is not active");
        if (milestone.getStatus() != Milestone.MilestoneStatus.IN_PROGRESS && milestone.getStatus() != Milestone.MilestoneStatus.REVISION_REQUESTED)
            throw new IllegalArgumentException("This milestone cannot be submitted in its current state");
        var submitter = users.findByEmailIgnoreCase(email).orElseThrow();
        MilestoneSubmission submission = new MilestoneSubmission(); submission.setMilestone(milestone); submission.setSubmittedBy(submitter);
        submission.setDescription(request.description().trim()); submission.setComments(request.comments()); submissions.save(submission);
        var before = milestone.getStatus(); milestone.setStatus(Milestone.MilestoneStatus.SUBMITTED);
        record(milestone, before, milestone.getStatus(), email, request.comments());
        notifications.notify(milestone.getAssignment().getClient(),"WORK_SUBMITTED","MILESTONE",milestone.getId(),"Work was submitted for "+milestone.getTitle());
        audit.record(email,"WORK_SUBMITTED","MILESTONE",milestone.getId());
        return response(milestone);
    }
    @Transactional
    public MilestoneResponse approve(Long id, String email) {
        Milestone milestone = milestones.findById(id).orElseThrow(() -> new IllegalArgumentException("Milestone not found"));
        if (!milestone.getAssignment().getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Milestone not found");
        if (milestone.getStatus() != Milestone.MilestoneStatus.SUBMITTED) throw new IllegalArgumentException("Only submitted work can be approved");
        var before = milestone.getStatus(); milestone.setStatus(Milestone.MilestoneStatus.APPROVED);
        record(milestone, before, milestone.getStatus(), email, "Work approved");
        notifications.notify(milestone.getAssignment().getFreelancer(),"WORK_APPROVED","MILESTONE",milestone.getId(),"Work was approved for "+milestone.getTitle());
        audit.record(email,"MILESTONE_APPROVED","MILESTONE",milestone.getId());
        Payment payment = new Payment(); payment.setAssignment(milestone.getAssignment()); payment.setMilestone(milestone); payment.setAmount(milestone.getAmount());
        payment.setStatus(Payment.PaymentStatus.PENDING); payments.save(payment);
        var all = milestones.findByAssignmentIdOrderBySequenceNumber(milestone.getAssignment().getId());
        if (!all.isEmpty() && all.stream().allMatch(m -> m.getStatus() == Milestone.MilestoneStatus.APPROVED)) {
            Assignment assignment = milestone.getAssignment(); assignment.setStatus(Assignment.AssignmentStatus.COMPLETED); assignment.setCompletedAt(java.time.Instant.now());
            assignment.getProject().setStatus(Project.ProjectStatus.COMPLETED);
        }
        return response(milestone);
    }
    @Transactional
    public MilestoneResponse requestRevision(Long id, String email, RevisionRequest request) {
        Milestone milestone = milestones.findById(id).orElseThrow(() -> new IllegalArgumentException("Milestone not found"));
        if (!milestone.getAssignment().getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Milestone not found");
        if (milestone.getStatus() != Milestone.MilestoneStatus.SUBMITTED) throw new IllegalArgumentException("Only submitted work can be sent for revision");
        var before = milestone.getStatus(); milestone.setStatus(Milestone.MilestoneStatus.REVISION_REQUESTED);
        record(milestone, before, milestone.getStatus(), email, request.comment().trim());
        notifications.notify(milestone.getAssignment().getFreelancer(),"REVISION_REQUESTED","MILESTONE",milestone.getId(),"The client requested revisions for "+milestone.getTitle());
        audit.record(email,"REVISION_REQUESTED","MILESTONE",milestone.getId());
        return response(milestone);
    }
    private void ensureParticipant(Assignment a, String email) {
        if (!a.getClient().getEmail().equalsIgnoreCase(email) && !a.getFreelancer().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Assignment not found");
    }
    private void ensureAssignedFreelancer(Milestone milestone, String email) {
        if (!milestone.getAssignment().getFreelancer().getEmail().equalsIgnoreCase(email))
            throw new IllegalArgumentException("Milestone not found");
    }
    private void record(Milestone milestone, Milestone.MilestoneStatus from, Milestone.MilestoneStatus to, String email, String comment) {
        MilestoneHistory event = new MilestoneHistory(); event.setMilestone(milestone); event.setChangedBy(users.findByEmailIgnoreCase(email).orElseThrow());
        event.setFromStatus(from == null ? null : from.name()); event.setToStatus(to.name()); event.setComment(comment); history.save(event);
    }
    private MilestoneResponse response(Milestone m) {
        var submissionSummaries = submissions.findByMilestoneIdOrderBySubmittedAtDesc(m.getId()).stream()
                .map(s -> new MilestoneResponse.SubmissionSummary(s.getId(), s.getDescription(), s.getComments(), s.getSubmittedBy().getFullName(), s.getSubmittedAt(),submissionFiles.findBySubmissionId(s.getId()).stream().map(f->new MilestoneResponse.FileSummary(f.getId(),f.getOriginalName(),"/submissions/"+s.getId()+"/files/"+f.getId()+"/download")).toList())).toList();
        var progressUpdates = history.findByMilestoneIdOrderByChangedAtAsc(m.getId()).stream()
                .map(event -> new MilestoneResponse.ProgressUpdate(event.getFromStatus(), event.getToStatus(), event.getComment(), event.getChangedBy().getFullName(), event.getChangedAt())).toList();
        return new MilestoneResponse(m.getId(), m.getAssignment().getId(), m.getTitle(), m.getDescription(), m.getDueDate(), m.getAmount(), m.getSequenceNumber(), m.getStatus(), submissionSummaries, progressUpdates);
    }
}
