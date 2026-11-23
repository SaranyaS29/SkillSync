package com.freelanceplatform.controller;

import com.freelanceplatform.dto.file.FileResponse;
import com.freelanceplatform.service.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController @RequestMapping("/api")
public class AttachmentController {
    private final ProjectAttachmentService projectFiles;private final SubmissionFileService submissionFiles;
    public AttachmentController(ProjectAttachmentService projectFiles,SubmissionFileService submissionFiles){this.projectFiles=projectFiles;this.submissionFiles=submissionFiles;}
    @PostMapping(value="/projects/{projectId}/attachments",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasRole('CLIENT')")
    public FileResponse uploadProject(@PathVariable Long projectId,@RequestPart("file") MultipartFile file,Authentication auth){return projectFiles.upload(projectId,auth.getName(),file);}
    @GetMapping("/projects/{projectId}/attachments")
    public List<FileResponse> list(@PathVariable Long projectId,Authentication auth){return projectFiles.list(projectId,authenticatedEmail(auth));}
    @GetMapping("/projects/{projectId}/attachments/{fileId}/download")
    public ResponseEntity<ByteArrayResource> projectDownload(@PathVariable Long projectId,@PathVariable Long fileId,Authentication auth){var f=projectFiles.download(projectId,fileId,authenticatedEmail(auth));return download(f.name(),f.bytes());}
    @PostMapping(value="/submissions/{submissionId}/files",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasRole('FREELANCER')")
    public FileResponse uploadSubmission(@PathVariable Long submissionId,@RequestPart("file") MultipartFile file,Authentication auth){return submissionFiles.upload(submissionId,auth.getName(),file);}
    @GetMapping("/submissions/{submissionId}/files") @PreAuthorize("hasAnyRole('CLIENT','FREELANCER')")
    public List<FileResponse> listSubmissionFiles(@PathVariable Long submissionId,Authentication auth){return submissionFiles.list(submissionId,auth.getName());}
    @GetMapping("/submissions/{submissionId}/files/{fileId}/download") @PreAuthorize("hasAnyRole('CLIENT','FREELANCER')")
    public ResponseEntity<ByteArrayResource> submissionDownload(@PathVariable Long submissionId,@PathVariable Long fileId,Authentication auth){var f=submissionFiles.download(submissionId,fileId,auth.getName());return download(f.name(),f.bytes());}
    private ResponseEntity<ByteArrayResource> download(String name,byte[] bytes){
        ContentDisposition disposition=ContentDisposition.attachment().filename(name,StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(bytes.length).header(HttpHeaders.CONTENT_DISPOSITION,disposition.toString()).body(new ByteArrayResource(bytes));
    }
    private String authenticatedEmail(Authentication auth){return auth==null||!auth.isAuthenticated()||auth instanceof AnonymousAuthenticationToken?null:auth.getName();}
}
