package com.freelanceplatform.repository;
import com.freelanceplatform.entity.FreelancerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, Long> {
    @EntityGraph(attributePaths={"user", "skills"})
    java.util.List<FreelancerProfile> findByAvailabilityNot(String availability, Pageable pageable);
    @Query("select distinct f from FreelancerProfile f left join f.skills s where f.availability <> 'UNAVAILABLE' " +
            "and (:availability is null or f.availability = :availability) " +
            "and (:skill is null or lower(s.name) = lower(:skill)) " +
            "and (:minRate is null or f.hourlyRate >= :minRate) " +
            "and (:maxRate is null or f.hourlyRate <= :maxRate) " +
            "and (:minExperience is null or f.experienceYears >= :minExperience) " +
            "and (:maxExperience is null or f.experienceYears <= :maxExperience) " +
            "and (:minRating is null or coalesce((select avg(r.rating) from Review r where r.reviewee.id = f.user.id), 0.0) >= :minRating)")
    Page<FreelancerProfile> search(@Param("skill") String skill, @Param("minRate") BigDecimal minRate,
                                   @Param("maxRate") BigDecimal maxRate, @Param("minExperience") Integer minExperience,
                                   @Param("maxExperience") Integer maxExperience, @Param("availability") String availability,
                                   @Param("minRating") Double minRating, Pageable pageable);
}
