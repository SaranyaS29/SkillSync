package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReportRepository extends JpaRepository<Report, Long> {}
