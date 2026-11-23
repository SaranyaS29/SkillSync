package com.freelanceplatform.repository;
import com.freelanceplatform.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    @Query("select count(distinct u) from UserAccount u join u.roles r where r.name = :role") long countByRoleName(@Param("role") String role);
}
