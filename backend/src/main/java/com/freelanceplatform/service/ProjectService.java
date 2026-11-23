package com.freelanceplatform.service;

import com.freelanceplatform.dto.project.ProjectRequest;
import com.freelanceplatform.dto.project.ProjectResponse;
import com.freelanceplatform.entity.Project;
import com.freelanceplatform.repository.ProjectRepository;
import com.freelanceplatform.repository.FreelancerProfileRepository;
import com.freelanceplatform.repository.SkillRepository;
import com.freelanceplatform.repository.ProposalRepository;
import com.freelanceplatform.repository.UserAccountRepository;
import com.freelanceplatform.repository.AssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final UserAccountRepository users;
    private final FreelancerProfileRepository freelancerProfiles;
    private final SkillRepository skills;
    private final ProposalRepository proposals;
    private final AuditLogService audit;
    private final AssignmentRepository assignments;
    private final NotificationService notifications;
    public ProjectService(ProjectRepository projects, UserAccountRepository users, FreelancerProfileRepository freelancerProfiles, SkillRepository skills, ProposalRepository proposals,AuditLogService audit,AssignmentRepository assignments,NotificationService notifications) { this.projects = projects; this.users = users; this.freelancerProfiles = freelancerProfiles; this.skills=skills; this.proposals=proposals;this.audit=audit;this.assignments=assignments;this.notifications=notifications; }

    @Transactional
    public ProjectResponse create(String email, ProjectRequest request) {
        if (request.budgetMax().compareTo(request.budgetMin()) < 0) throw new IllegalArgumentException("Maximum budget must be at least the minimum budget");
        Project project = new Project();
        project.setClient(users.findByEmailIgnoreCase(email).orElseThrow());
        apply(project, request);
        project.setStatus(request.publish() ? Project.ProjectStatus.OPEN : Project.ProjectStatus.DRAFT);
        project=projects.save(project);audit.record(email,"PROJECT_CREATED","PROJECT",project.getId());return toResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> browse(String title, String skill, java.math.BigDecimal minBudget,
            java.math.BigDecimal maxBudget, String experienceLevel, Project.ProjectType projectType, Pageable pageable) {
        if (minBudget != null && maxBudget != null && minBudget.compareTo(maxBudget) > 0)
            throw new IllegalArgumentException("Minimum budget must not exceed maximum budget");
        Specification<Project> spec = (root, query, cb) -> cb.equal(root.get("status"), Project.ProjectStatus.OPEN);
        if (title != null && !title.isBlank()) {
            String term = "%" + title.trim().toLowerCase(java.util.Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), term));
        }
        if (skill != null && !skill.isBlank()) {
            String term = "%" + skill.trim().toLowerCase(java.util.Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                var requiredSkill = root.join("requiredSkills", jakarta.persistence.criteria.JoinType.INNER);
                return cb.like(cb.lower(requiredSkill.get("name")), term);
            });
        }
        if (minBudget != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("budgetMax"), minBudget));
        if (maxBudget != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("budgetMin"), maxBudget));
        if (experienceLevel != null && !experienceLevel.isBlank())
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("experienceLevel")), experienceLevel.trim().toUpperCase(java.util.Locale.ROOT)));
        if (projectType != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("projectType"), projectType));
        return projects.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> mine(String email, Pageable pageable) {
        var client = users.findByEmailIgnoreCase(email).orElseThrow();
        return projects.findByClientId(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public java.util.List<com.freelanceplatform.dto.project.FreelancerMatchResponse> matches(Long projectId, String email) {
        Project project = projects.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        if (!project.getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Project not found");
        var required = project.getRequiredSkills().stream().map(skill -> skill.getName().toLowerCase()).collect(java.util.stream.Collectors.toSet());
        return freelancerProfiles.findByAvailabilityNot("UNAVAILABLE", org.springframework.data.domain.PageRequest.of(0, 100)).stream()
                .filter(profile -> !profile.getUserId().equals(project.getClient().getId()))
                .map(profile -> {
                    var freelancerSkills = profile.getSkills().stream().map(skill -> skill.getName().toLowerCase()).collect(java.util.stream.Collectors.toSet());
                    int skillScore = required.isEmpty() ? 50 : (int)Math.round(100.0 * required.stream().filter(freelancerSkills::contains).count() / required.size());
                    int years = profile.getExperienceYears() == null ? 0 : profile.getExperienceYears();
                    int experienceScore = switch (project.getExperienceLevel() == null ? "" : project.getExperienceLevel().toUpperCase()) {
                        case "ENTRY", "ENTRY_LEVEL" -> years <= 2 ? 100 : 70;
                        case "INTERMEDIATE" -> years >= 2 && years <= 8 ? 100 : years > 8 ? 75 : 60;
                        case "EXPERT", "ADVANCED" -> years >= 5 ? 100 : Math.min(80, years * 16);
                        default -> Math.min(100, years * 20);
                    };
                    int budgetScore;
                    String budgetNote;
                    if (profile.getHourlyRate() == null) {
                        budgetScore = 50;
                        budgetNote = "Budget score is neutral because the freelancer has not set an hourly rate.";
                    } else if (project.getProjectType() == Project.ProjectType.HOURLY) {
                        var rate = profile.getHourlyRate();
                        if (rate.compareTo(project.getBudgetMin()) >= 0 && rate.compareTo(project.getBudgetMax()) <= 0) budgetScore = 100;
                        else if (rate.compareTo(project.getBudgetMin()) < 0) budgetScore = Math.max(0, 100 - rateDistance(rate, project.getBudgetMin()));
                        else budgetScore = Math.max(0, 100 - rateDistance(project.getBudgetMax(), rate));
                        budgetNote = "Hourly rate compared with the project's hourly budget range.";
                    } else {
                        var rate = profile.getHourlyRate();
                        var effectiveHourlyCap = project.getBudgetMax().divide(java.math.BigDecimal.valueOf(40), 2, java.math.RoundingMode.HALF_UP);
                        budgetScore = rate.compareTo(effectiveHourlyCap) <= 0 ? 100 : Math.max(0,
                                effectiveHourlyCap.multiply(java.math.BigDecimal.valueOf(100)).divide(rate, 0, java.math.RoundingMode.HALF_UP).intValue());
                        budgetNote = "Fixed-price affordability uses an estimated 40-hour effort; the budget's effective hourly cap is INR " + effectiveHourlyCap + " per hour.";
                    }
                    int score = (int)Math.round(skillScore * .70 + experienceScore * .20 + budgetScore * .10);
                    String skillNote = required.isEmpty() ? "No required skills were specified." : "Matches " + required.stream().filter(freelancerSkills::contains).count() + " of " + required.size() + " required skills.";
                    String experienceNote = "Experience score uses the project's experience level and the freelancer's listed years.";
                    return new com.freelanceplatform.dto.project.FreelancerMatchResponse(profile.getUserId(), profile.getUser().getFullName(), profile.getHeadline(), profile.getHourlyRate(), profile.getExperienceYears(), profile.getSkills().stream().map(skill -> skill.getName()).sorted().toList(), score, skillScore, experienceScore, budgetScore, skillNote + " " + experienceNote + " " + budgetNote);
                }).sorted(java.util.Comparator.comparingInt(com.freelanceplatform.dto.project.FreelancerMatchResponse::score).reversed()).toList();
    }

    private int rateDistance(java.math.BigDecimal lower, java.math.BigDecimal higher) {
        if (lower.signum() == 0) return 100;
        return Math.min(100, higher.subtract(lower).multiply(java.math.BigDecimal.valueOf(100)).divide(lower, 0, java.math.RoundingMode.HALF_UP).intValue());
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id, String email) {
        Project project = projects.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        boolean isClient = project.getClient().getEmail().equalsIgnoreCase(email);
        boolean isAssignedFreelancer = assignments.findByProjectId(id)
                .map(assignment -> assignment.getFreelancer().getEmail().equalsIgnoreCase(email))
                .orElse(false);
        if (project.getStatus() != Project.ProjectStatus.OPEN && !isClient && !isAssignedFreelancer) {
            throw new IllegalArgumentException("Project not found");
        }
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse update(Long id, String email, ProjectRequest request) {
        Project project = projects.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        ensureOwner(project, email);
        if (project.getStatus() != Project.ProjectStatus.DRAFT && project.getStatus() != Project.ProjectStatus.OPEN)
            throw new IllegalArgumentException("Only draft or open projects can be edited");
        if (request.budgetMax().compareTo(request.budgetMin()) < 0) throw new IllegalArgumentException("Maximum budget must be at least the minimum budget");
        apply(project, request);
        project.setStatus(request.publish() ? Project.ProjectStatus.OPEN : Project.ProjectStatus.DRAFT);
        audit.record(email,"PROJECT_UPDATED","PROJECT",project.getId());
        return toResponse(project);
    }

    @Transactional
    public void delete(Long id, String email) {
        Project project = projects.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        ensureOwner(project, email);
        if (project.getStatus() != Project.ProjectStatus.DRAFT) throw new IllegalArgumentException("Only draft projects can be deleted");
        if (proposals.existsByProjectId(id)) throw new IllegalArgumentException("A project with proposal history cannot be deleted");
        audit.record(email,"PROJECT_DELETED","PROJECT",id);
        projects.delete(project);
    }

    @Transactional
    public ProjectResponse cancel(Long id, String email) {
        Project project = projects.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        ensureOwner(project, email);
        if (project.getStatus() != Project.ProjectStatus.DRAFT && project.getStatus() != Project.ProjectStatus.OPEN)
            throw new IllegalArgumentException("Only draft or open projects can be cancelled");
        if (assignments.findByProjectId(id).isPresent())
            throw new IllegalArgumentException("A project with an accepted proposal cannot be cancelled");
        project.setStatus(Project.ProjectStatus.CANCELLED);
        proposals.findByProjectId(id).stream()
                .filter(proposal -> proposal.getStatus() == com.freelanceplatform.entity.Proposal.ProposalStatus.PENDING)
                .forEach(proposal -> {
                    proposal.setStatus(com.freelanceplatform.entity.Proposal.ProposalStatus.REJECTED);
                    notifications.notify(proposal.getFreelancer(), "PROJECT_CANCELLED", "PROJECT", id, "The client cancelled the project: " + project.getTitle());
                });
        audit.record(email, "PROJECT_CANCELLED", "PROJECT", id);
        return toResponse(project);
    }

    private void ensureOwner(Project project, String email) {
        if (!project.getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Project not found");
    }
    private void apply(Project p, ProjectRequest r) {
        p.setTitle(r.title().trim()); p.setDescription(r.description().trim()); p.setProjectType(r.projectType());
        p.setBudgetMin(r.budgetMin()); p.setBudgetMax(r.budgetMax()); p.setExperienceLevel(r.experienceLevel());
        p.setCategory(r.category()); p.setDeadline(r.deadline());
        var required = new java.util.LinkedHashSet<com.freelanceplatform.entity.Skill>();
        if (r.requiredSkills() != null) for (String raw : r.requiredSkills()) {
            String name = raw.trim();
            if (!name.isEmpty()) required.add(skills.findByNameIgnoreCase(name).orElseGet(() -> { var skill = new com.freelanceplatform.entity.Skill(); skill.setName(name); return skills.save(skill); }));
        }
        p.setRequiredSkills(required);
    }
    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getClient().getId(), p.getTitle(), p.getDescription(), p.getProjectType(),
                p.getStatus(), p.getBudgetMin(), p.getBudgetMax(), p.getExperienceLevel(), p.getCategory(), p.getDeadline(),
                p.getRequiredSkills().stream().map(com.freelanceplatform.entity.Skill::getName).sorted().toList(), p.getCreatedAt());
    }
}
