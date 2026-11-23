package com.freelanceplatform.service;

import com.freelanceplatform.dto.file.FileResponse;
import com.freelanceplatform.entity.SubmissionFile;
import com.freelanceplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SubmissionFileService {
    public record Download(String name,byte[] bytes){}
    private final MilestoneSubmissionRepository submissions;private final SubmissionFileRepository submissionFiles;private final UserAccountRepository users;private final FileStorageService files;
    public SubmissionFileService(MilestoneSubmissionRepository submissions,SubmissionFileRepository submissionFiles,UserAccountRepository users,FileStorageService files){this.submissions=submissions;this.submissionFiles=submissionFiles;this.users=users;this.files=files;}
    @Transactional
    public FileResponse upload(Long submissionId,String email,MultipartFile file){
        var submission=submissions.findById(submissionId).orElseThrow(()->new IllegalArgumentException("Submission not found"));
        if(!submission.getMilestone().getAssignment().getFreelancer().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Submission not found");
        var stored=files.store(file);
        try{SubmissionFile saved=new SubmissionFile();saved.setSubmission(submission);saved.setStorageKey(stored.key());saved.setOriginalName(stored.originalName());saved=submissionFiles.save(saved);return new FileResponse(saved.getId(),saved.getOriginalName(),"/submissions/"+submissionId+"/files/"+saved.getId()+"/download",submission.getSubmittedAt());}
        catch(RuntimeException ex){files.delete(stored.key());throw ex;}
    }
    @Transactional(readOnly=true)
    public Download download(Long submissionId,Long fileId,String email){
        var submission=submissions.findById(submissionId).orElseThrow(()->new IllegalArgumentException("Submission not found"));
        var assignment=submission.getMilestone().getAssignment();
        if(!assignment.getFreelancer().getEmail().equalsIgnoreCase(email)&&!assignment.getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Submission not found");
        var file=submissionFiles.findByIdAndSubmissionId(fileId,submissionId).orElseThrow(()->new IllegalArgumentException("File not found"));
        return new Download(file.getOriginalName(),files.read(file.getStorageKey()));
    }
    @Transactional(readOnly=true)
    public java.util.List<FileResponse> list(Long submissionId,String email){
        var submission=submissions.findById(submissionId).orElseThrow(()->new IllegalArgumentException("Submission not found"));
        var assignment=submission.getMilestone().getAssignment();
        if(!assignment.getFreelancer().getEmail().equalsIgnoreCase(email)&&!assignment.getClient().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Submission not found");
        return submissionFiles.findBySubmissionId(submissionId).stream().map(f->new FileResponse(f.getId(),f.getOriginalName(),"/submissions/"+submissionId+"/files/"+f.getId()+"/download",submission.getSubmittedAt())).toList();
    }
}
