package com.freelanceplatform.service;

import com.freelanceplatform.dto.file.FileResponse;
import com.freelanceplatform.entity.ProjectAttachment;
import com.freelanceplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public class ProjectAttachmentService {
    public record Download(String name,byte[] bytes){}
    private final ProjectAttachmentRepository attachments; private final ProjectRepository projects;
    private final AssignmentRepository assignments; private final UserAccountRepository users; private final FileStorageService files;
    public ProjectAttachmentService(ProjectAttachmentRepository attachments,ProjectRepository projects,AssignmentRepository assignments,UserAccountRepository users,FileStorageService files){this.attachments=attachments;this.projects=projects;this.assignments=assignments;this.users=users;this.files=files;}
    @Transactional
    public FileResponse upload(Long projectId,String email,MultipartFile file){
        var project=projects.findByIdForUpdate(projectId).orElseThrow(()->new IllegalArgumentException("Project not found"));
        var user=users.findByEmailIgnoreCase(email).orElseThrow();
        if(!project.getClient().getId().equals(user.getId())) throw new IllegalArgumentException("Project not found");
        if(project.getStatus()!=com.freelanceplatform.entity.Project.ProjectStatus.DRAFT&&project.getStatus()!=com.freelanceplatform.entity.Project.ProjectStatus.OPEN) throw new IllegalArgumentException("Attachments cannot be added to this project");
        var stored=files.store(file);
        try { ProjectAttachment a=new ProjectAttachment();a.setProject(project);a.setUploadedBy(user);a.setStorageKey(stored.key());a.setOriginalName(stored.originalName());a=attachments.save(a);return response(a); }
        catch(RuntimeException ex){files.delete(stored.key());throw ex;}
    }
    @Transactional(readOnly=true)
    public List<FileResponse> list(Long projectId,String email){
        var project=projects.findById(projectId).orElseThrow(()->new IllegalArgumentException("Project not found"));authorize(project,email);
        return attachments.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(this::response).toList();
    }
    @Transactional(readOnly=true)
    public Download download(Long projectId,Long fileId,String email){
        var project=projects.findById(projectId).orElseThrow(()->new IllegalArgumentException("Project not found"));authorize(project,email);
        var attachment=attachments.findByIdAndProjectId(fileId,projectId).orElseThrow(()->new IllegalArgumentException("File not found"));
        return new Download(attachment.getOriginalName(),files.read(attachment.getStorageKey()));
    }
    private void authorize(com.freelanceplatform.entity.Project p,String email){
        if(p.getClient().getEmail().equalsIgnoreCase(email)) return;
        var assignment=assignments.findByProjectId(p.getId()).orElse(null);
        if(assignment!=null&&(assignment.getFreelancer().getEmail().equalsIgnoreCase(email)||assignment.getClient().getEmail().equalsIgnoreCase(email))) return;
        if(p.getStatus()==com.freelanceplatform.entity.Project.ProjectStatus.OPEN) return;
        throw new IllegalArgumentException("Project not found");
    }
    private FileResponse response(ProjectAttachment a){return new FileResponse(a.getId(),a.getOriginalName(),"/projects/"+a.getProject().getId()+"/attachments/"+a.getId()+"/download",a.getCreatedAt());}
}
