package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByProjectId(Long projectId);
    List<Proposal> findByFreelancerId(Long freelancerId);
    boolean existsByProjectId(Long projectId);
    long countByStatus(Proposal.ProposalStatus status);
    boolean existsByProjectIdAndFreelancerIdAndStatusIn(Long projectId, Long freelancerId, List<Proposal.ProposalStatus> statuses);
}
