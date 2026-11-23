package com.freelanceplatform.service;

import com.freelanceplatform.dto.proposal.ProposalRequest;
import com.freelanceplatform.dto.proposal.ProposalResponse;
import com.freelanceplatform.entity.Assignment;
import com.freelanceplatform.entity.Project;
import com.freelanceplatform.entity.Proposal;
import com.freelanceplatform.repository.AssignmentRepository;
import com.freelanceplatform.repository.ProjectRepository;
import com.freelanceplatform.repository.ProposalRepository;
import com.freelanceplatform.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProposalService {
    private static final List<Proposal.ProposalStatus> ACTIVE = List.of(Proposal.ProposalStatus.PENDING, Proposal.ProposalStatus.ACCEPTED);
    private final ProposalRepository proposals;
    private final ProjectRepository projects;
    private final AssignmentRepository assignments;
    private final UserAccountRepository users;
    private final NotificationService notifications;
    private final AuditLogService audit;
    public ProposalService(ProposalRepository proposals, ProjectRepository projects, AssignmentRepository assignments, UserAccountRepository users, NotificationService notifications,AuditLogService audit) {
        this.proposals = proposals; this.projects = projects; this.assignments = assignments; this.users = users; this.notifications=notifications;this.audit=audit;
    }

    @Transactional
    public ProposalResponse submit(Long projectId, String email, ProposalRequest request) {
        Project project = projects.findByIdForUpdate(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        if (project.getStatus() != Project.ProjectStatus.OPEN) throw new IllegalArgumentException("This project is not accepting proposals");
        var freelancer = users.findByEmailIgnoreCase(email).orElseThrow();
        if (project.getClient().getId().equals(freelancer.getId())) throw new IllegalArgumentException("You cannot propose to your own project");
        if (proposals.existsByProjectIdAndFreelancerIdAndStatusIn(projectId, freelancer.getId(), ACTIVE))
            throw new IllegalArgumentException("You already have an active proposal for this project");
        Proposal proposal = new Proposal(); proposal.setProject(project); proposal.setFreelancer(freelancer);
        proposal.setCoverLetter(request.coverLetter().trim()); proposal.setProposedBudget(request.proposedBudget());
        proposal.setCompletionDays(request.completionDays()); proposal.setStatus(Proposal.ProposalStatus.PENDING);
        if (request.portfolioLinks() != null) proposal.setPortfolioLinks(request.portfolioLinks().stream().map(String::trim).toList());
        Proposal saved=proposals.save(proposal);
        notifications.notify(project.getClient(),"PROPOSAL_RECEIVED","PROPOSAL",saved.getId(),"A freelancer submitted a proposal for "+project.getTitle());
        audit.record(email,"PROPOSAL_SUBMITTED","PROPOSAL",saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> mine(String email) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        return proposals.findByFreelancerId(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> forProject(Long projectId, String email) {
        Project project = projects.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        if (!project.getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Project not found");
        return proposals.findByProjectId(projectId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProposalResponse decide(Long proposalId, String email, boolean accept) {
        Proposal proposal = proposals.findById(proposalId).orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        Project project = projects.findByIdForUpdate(proposal.getProject().getId()).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        if (!project.getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Proposal not found");
        if (project.getStatus() != Project.ProjectStatus.OPEN || proposal.getStatus() != Proposal.ProposalStatus.PENDING)
            throw new IllegalArgumentException("This proposal is no longer actionable");
        if (accept) {
            proposal.setStatus(Proposal.ProposalStatus.ACCEPTED);
            Assignment assignment = new Assignment(); assignment.setProject(project); assignment.setProposal(proposal);
            assignment.setClient(project.getClient()); assignment.setFreelancer(proposal.getFreelancer());
            assignments.save(assignment);
            project.setStatus(Project.ProjectStatus.IN_PROGRESS);
            notifications.notify(proposal.getFreelancer(),"PROPOSAL_ACCEPTED","PROJECT",project.getId(),"Your proposal was accepted for "+project.getTitle());
            proposals.findByProjectId(project.getId()).stream()
                    .filter(other -> !other.getId().equals(proposalId) && other.getStatus() == Proposal.ProposalStatus.PENDING)
                    .forEach(other -> { other.setStatus(Proposal.ProposalStatus.REJECTED); notifications.notify(other.getFreelancer(),"PROPOSAL_REJECTED","PROJECT",project.getId(),"The client selected another proposal for "+project.getTitle()); });
        } else {
            proposal.setStatus(Proposal.ProposalStatus.REJECTED);
            notifications.notify(proposal.getFreelancer(),"PROPOSAL_REJECTED","PROJECT",project.getId(),"Your proposal was declined for "+project.getTitle());
        }
        audit.record(email,accept?"PROPOSAL_ACCEPTED":"PROPOSAL_REJECTED","PROPOSAL",proposalId);
        return toResponse(proposal);
    }

    @Transactional
    public void withdraw(Long proposalId, String email) {
        Proposal proposal = proposals.findById(proposalId).orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        if (!proposal.getFreelancer().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Proposal not found");
        if (proposal.getStatus() != Proposal.ProposalStatus.PENDING) throw new IllegalArgumentException("Only pending proposals can be withdrawn");
        proposal.setStatus(Proposal.ProposalStatus.WITHDRAWN);
    }

    private ProposalResponse toResponse(Proposal p) {
        return new ProposalResponse(p.getId(), p.getProject().getId(), p.getProject().getTitle(), p.getFreelancer().getId(), p.getFreelancer().getFullName(),
                p.getCoverLetter(), p.getProposedBudget(), p.getCompletionDays(), List.copyOf(p.getPortfolioLinks()), p.getStatus(), p.getCreatedAt());
    }
}
