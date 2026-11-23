package com.freelanceplatform.dto.project;

import com.freelanceplatform.entity.Project;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ProjectResponse(Long id, Long clientId, String title, String description,
                              Project.ProjectType projectType, Project.ProjectStatus status,
                              BigDecimal budgetMin, BigDecimal budgetMax, String experienceLevel,
                              String category, LocalDate deadline, List<String> requiredSkills, Instant createdAt) {}
