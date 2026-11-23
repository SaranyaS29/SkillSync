package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Page<Project> findByStatus(Project.ProjectStatus status, Pageable pageable);
    Page<Project> findByClientId(Long clientId, Pageable pageable);
    long countByStatus(Project.ProjectStatus status);
    Page<Project> findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(Project.ProjectStatus status1, String title, Project.ProjectStatus status2, String description, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Project p where p.id = :id") Optional<Project> findByIdForUpdate(@Param("id") Long id);
}
