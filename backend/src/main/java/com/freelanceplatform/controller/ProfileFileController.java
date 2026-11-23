package com.freelanceplatform.controller;

import com.freelanceplatform.service.ProfileFileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/freelancers")
public class ProfileFileController {
    private final ProfileFileService service;
    public ProfileFileController(ProfileFileService service){this.service=service;}
    @PostMapping(value="/profile/photo",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasRole('FREELANCER')")
    public java.util.Map<String,String> photo(@RequestPart("file") MultipartFile file,Authentication auth){return java.util.Map.of("message","Profile photo updated","fileKey",service.uploadPhoto(auth.getName(),file));}
    @PostMapping(value="/profile/resume",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasRole('FREELANCER')")
    public java.util.Map<String,String> resume(@RequestPart("file") MultipartFile file,Authentication auth){return java.util.Map.of("message","Resume uploaded","fileKey",service.uploadResume(auth.getName(),file));}
    @GetMapping("/{id}/photo") public ResponseEntity<ByteArrayResource> publicPhoto(@PathVariable Long id){var f=service.photo(id);return ResponseEntity.ok().contentType(media(f.key())).body(new ByteArrayResource(f.bytes()));}
    @GetMapping("/profile/resume") @PreAuthorize("hasRole('FREELANCER')") public ResponseEntity<ByteArrayResource> privateResume(Authentication auth){var f=service.resume(auth.getName());return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(f.name()+"."+extension(f.key())).build().toString()).body(new ByteArrayResource(f.bytes()));}
    private MediaType media(String key){return key.toLowerCase().endsWith(".png")?MediaType.IMAGE_PNG:MediaType.IMAGE_JPEG;}
    private String extension(String key){int i=key.lastIndexOf('.');return i<0?"bin":key.substring(i+1);}
}
