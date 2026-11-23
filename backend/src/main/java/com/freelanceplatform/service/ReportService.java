package com.freelanceplatform.service;

import com.freelanceplatform.dto.report.*;
import com.freelanceplatform.entity.Report;
import com.freelanceplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private final ReportRepository reports; private final UserAccountRepository users; private final ProjectRepository projects;
    public ReportService(ReportRepository reports,UserAccountRepository users,ProjectRepository projects){this.reports=reports;this.users=users;this.projects=projects;}
    @Transactional
    public ReportResponse create(String email,ReportRequest request){
        if((request.targetUserId()==null)==(request.projectId()==null)) throw new IllegalArgumentException("Report exactly one user or project");
        Report report=new Report(); report.setReporter(users.findByEmailIgnoreCase(email).orElseThrow()); report.setReason(request.reason().trim()); report.setStatus("OPEN");
        if(request.targetUserId()!=null) report.setTargetUser(users.findById(request.targetUserId()).orElseThrow(()->new IllegalArgumentException("User not found")));
        if(request.projectId()!=null) report.setProject(projects.findById(request.projectId()).orElseThrow(()->new IllegalArgumentException("Project not found")));
        report=reports.save(report); return response(report);
    }
    private ReportResponse response(Report r){return new ReportResponse(r.getId(),r.getReporter().getId(),r.getReporter().getFullName(),r.getTargetUser()==null?null:r.getTargetUser().getId(),r.getTargetUser()==null?null:r.getTargetUser().getFullName(),r.getProject()==null?null:r.getProject().getId(),r.getProject()==null?null:r.getProject().getTitle(),r.getStatus(),r.getReason(),r.getCreatedAt());}
}
