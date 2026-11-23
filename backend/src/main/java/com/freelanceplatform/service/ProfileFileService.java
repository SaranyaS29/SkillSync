package com.freelanceplatform.service;

import com.freelanceplatform.entity.FreelancerProfile;
import com.freelanceplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileFileService {
    public record Download(String name,String key,byte[] bytes){}
    private final UserAccountRepository users;private final FreelancerProfileRepository profiles;private final FileStorageService storage;
    public ProfileFileService(UserAccountRepository users,FreelancerProfileRepository profiles,FileStorageService storage){this.users=users;this.profiles=profiles;this.storage=storage;}
    @Transactional
    public String uploadPhoto(String email,MultipartFile file){return replace(email,file,true);}
    @Transactional
    public String uploadResume(String email,MultipartFile file){return replace(email,file,false);}
    @Transactional(readOnly=true)
    public Download photo(Long freelancerId){FreelancerProfile p=profiles.findById(freelancerId).orElseThrow(()->new IllegalArgumentException("Freelancer not found"));if(p.getProfileImageKey()==null)throw new IllegalArgumentException("Profile photo not found");return new Download("profile-photo",p.getProfileImageKey(),storage.read(p.getProfileImageKey()));}
    @Transactional(readOnly=true)
    public Download resume(String email){var u=users.findByEmailIgnoreCase(email).orElseThrow();FreelancerProfile p=profiles.findById(u.getId()).orElseThrow(()->new IllegalArgumentException("Freelancer profile not found"));if(p.getResumeKey()==null)throw new IllegalArgumentException("Resume not found");return new Download("resume",p.getResumeKey(),storage.read(p.getResumeKey()));}
    private String replace(String email,MultipartFile file,boolean photo){
        String name=file.getOriginalFilename()==null?"":file.getOriginalFilename().toLowerCase(java.util.Locale.ROOT);
        boolean allowed=photo?(name.endsWith(".png")||name.endsWith(".jpg")||name.endsWith(".jpeg")):(name.endsWith(".pdf")||name.endsWith(".doc")||name.endsWith(".docx"));
        if(!allowed)throw new IllegalArgumentException(photo?"Profile photos must be PNG or JPG":"Resumes must be PDF, DOC, or DOCX");
        var user=users.findByEmailIgnoreCase(email).orElseThrow();FreelancerProfile p=profiles.findById(user.getId()).orElseThrow(()->new IllegalArgumentException("Freelancer profile not found"));
        var stored=storage.store(file);String old=photo?p.getProfileImageKey():p.getResumeKey();if(photo)p.setProfileImageKey(stored.key());else p.setResumeKey(stored.key());if(old!=null)storage.delete(old);return stored.key();
    }
}
