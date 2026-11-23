package com.freelanceplatform.service;

import com.freelanceplatform.dto.assignment.AssignmentResponse;
import com.freelanceplatform.repository.AssignmentRepository;
import com.freelanceplatform.repository.ClientProfileRepository;
import com.freelanceplatform.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AssignmentService {
    private final AssignmentRepository assignments; private final UserAccountRepository users; private final ClientProfileRepository clientProfiles;
    public AssignmentService(AssignmentRepository assignments, UserAccountRepository users, ClientProfileRepository clientProfiles) { this.assignments=assignments; this.users=users; this.clientProfiles=clientProfiles; }
    @Transactional(readOnly=true)
    public List<AssignmentResponse> mine(String email, boolean client) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        var results = client ? assignments.findByClientId(user.getId()) : assignments.findByFreelancerId(user.getId());
        return results.stream().map(a -> {
            var clientProfile = clientProfiles.findById(a.getClient().getId()).orElse(null);
            return new AssignmentResponse(a.getId(), a.getProject().getId(), a.getProject().getTitle(),
                a.getClient().getId(), a.getClient().getFullName(), clientProfile == null ? null : clientProfile.getCompanyName(),
                clientProfile == null ? null : clientProfile.getCompanyDescription(), clientProfile == null ? null : clientProfile.getWebsite(),
                clientProfile == null ? null : clientProfile.getLocation(), a.getFreelancer().getId(), a.getFreelancer().getFullName(),
                a.getProposal().getProposedBudget(), a.getStatus(), a.getStartedAt(), a.getCompletedAt());
        }).toList();
    }
}
