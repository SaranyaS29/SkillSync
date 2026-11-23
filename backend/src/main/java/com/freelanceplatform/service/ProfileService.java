package com.freelanceplatform.service;

import com.freelanceplatform.dto.profile.*;
import com.freelanceplatform.entity.*;
import com.freelanceplatform.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ProfileService {
    private final UserAccountRepository users;
    private final FreelancerProfileRepository freelancers;
    private final ClientProfileRepository clients;
    private final SkillRepository skills;
    private final ReviewRepository reviews;
    private final PortfolioItemRepository portfolioItems;
    public ProfileService(UserAccountRepository users, FreelancerProfileRepository freelancers, ClientProfileRepository clients, SkillRepository skills,ReviewRepository reviews,PortfolioItemRepository portfolioItems) {
        this.users=users; this.freelancers=freelancers; this.clients=clients; this.skills=skills;this.reviews=reviews;this.portfolioItems=portfolioItems;
    }

    @Transactional(readOnly=true)
    public Page<FreelancerProfileResponse> searchFreelancers(String skill, BigDecimal minRate, BigDecimal maxRate,
            Integer minExperience, Integer maxExperience, String availability, Double minRating, Pageable pageable) {
        String selectedAvailability = availability == null || availability.isBlank() || availability.equalsIgnoreCase("ANY") ? null : availability.trim().toUpperCase();
        if (selectedAvailability != null && !List.of("AVAILABLE", "LIMITED").contains(selectedAvailability))
            throw new IllegalArgumentException("Availability must be AVAILABLE or LIMITED");
        return freelancers.search(skill == null || skill.isBlank() ? null : skill.trim(), minRate, maxRate,
                minExperience, maxExperience, selectedAvailability, minRating, pageable).map(this::freelancerResponse);
    }
    @Transactional(readOnly=true)
    public FreelancerProfileResponse freelancer(Long id) {
        return freelancers.findById(id).map(this::freelancerResponse).orElseThrow(() -> new IllegalArgumentException("Freelancer not found"));
    }
    @Transactional
    public FreelancerProfileResponse updateFreelancer(String email, FreelancerProfileRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow();
        FreelancerProfile profile = freelancers.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("Freelancer profile not found"));
        profile.setHeadline(trimToNull(request.headline())); profile.setDescription(trimToNull(request.description()));
        profile.setHourlyRate(request.hourlyRate()); profile.setExperienceYears(request.experienceYears());
        profile.setAvailability(request.availability() == null ? "AVAILABLE" : request.availability()); profile.setLocation(trimToNull(request.location()));
        var selected = new LinkedHashSet<Skill>();
        if (request.skills() != null) for (String raw : request.skills()) {
            String name = raw.trim();
            if (!name.isEmpty()) selected.add(skills.findByNameIgnoreCase(name).orElseGet(() -> { Skill skill = new Skill(); skill.setName(name); return skills.save(skill); }));
        }
        profile.setSkills(selected);
        if(request.portfolioItems()!=null){
            portfolioItems.deleteByFreelancerId(user.getId());
            var items=request.portfolioItems().stream().map(item->{PortfolioItem portfolio=new PortfolioItem();portfolio.setFreelancer(user);portfolio.setTitle(item.title().trim());portfolio.setDescription(trimToNull(item.description()));portfolio.setUrl(trimToNull(item.url()));return portfolio;}).toList();
            portfolioItems.saveAll(items);
        }
        return freelancerResponse(profile);
    }

    @Transactional(readOnly=true)
    public ClientProfileResponse client(String email) {
        UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow();
        ClientProfile profile = clients.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("Client profile not found"));
        return clientResponse(profile);
    }
    @Transactional
    public ClientProfileResponse updateClient(String email, ClientProfileRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow();
        ClientProfile profile = clients.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("Client profile not found"));
        profile.setCompanyName(trimToNull(request.companyName())); profile.setCompanyDescription(trimToNull(request.companyDescription()));
        profile.setWebsite(trimToNull(request.website())); profile.setLocation(trimToNull(request.location()));
        return clientResponse(profile);
    }
    private FreelancerProfileResponse freelancerResponse(FreelancerProfile p) {
        return new FreelancerProfileResponse(p.getUserId(), p.getUser().getFullName(), p.getHeadline(), p.getDescription(), p.getHourlyRate(),
                p.getExperienceYears(), p.getAvailability(), p.getLocation(), p.getSkills().stream().map(Skill::getName).sorted().toList(),reviews.averageForUser(p.getUserId()),reviews.countByRevieweeId(p.getUserId()),p.getProfileImageKey()==null?null:"/api/freelancers/"+p.getUserId()+"/photo",p.getResumeKey()!=null,portfolioItems.findByFreelancerIdOrderById(p.getUserId()).stream().map(i->new PortfolioItemRequest(i.getTitle(),i.getDescription(),i.getUrl())).toList());
    }
    private ClientProfileResponse clientResponse(ClientProfile p) {
        return new ClientProfileResponse(p.getUserId(), p.getUser().getFullName(), p.getUser().getEmail(), p.getCompanyName(), p.getCompanyDescription(), p.getWebsite(), p.getLocation(),reviews.averageForUser(p.getUserId()),reviews.countByRevieweeId(p.getUserId()));
    }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
