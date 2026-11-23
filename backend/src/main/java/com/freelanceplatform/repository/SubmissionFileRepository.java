package com.freelanceplatform.repository;
import com.freelanceplatform.entity.SubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SubmissionFileRepository extends JpaRepository<SubmissionFile,Long>{Optional<SubmissionFile> findByIdAndSubmissionId(Long id,Long submissionId);java.util.List<SubmissionFile> findBySubmissionId(Long submissionId);}
