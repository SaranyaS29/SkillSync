package com.freelanceplatform.service;

import com.freelanceplatform.entity.*;
import com.freelanceplatform.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Component @Profile("dev")
public class DevelopmentDataInitializer implements ApplicationRunner {
    private static final String DEMO_PASSWORD="DemoPass123!";
    private final UserAccountRepository users; private final RoleRepository roles; private final FreelancerProfileRepository freelancerProfiles;
    private final ClientProfileRepository clientProfiles; private final SkillRepository skills; private final ProjectRepository projects;
    private final ProposalRepository proposals; private final AssignmentRepository assignments; private final MilestoneRepository milestones;
    private final MilestoneSubmissionRepository submissions; private final PaymentRepository payments; private final NotificationRepository notifications;
    private final ConversationRepository conversations; private final MessageRepository messages; private final ReviewRepository reviews;
    private final PasswordEncoder encoder;
    public DevelopmentDataInitializer(UserAccountRepository users,RoleRepository roles,FreelancerProfileRepository freelancerProfiles,
            ClientProfileRepository clientProfiles,SkillRepository skills,ProjectRepository projects,ProposalRepository proposals,
            AssignmentRepository assignments,MilestoneRepository milestones,MilestoneSubmissionRepository submissions,PaymentRepository payments,
            NotificationRepository notifications,ConversationRepository conversations,MessageRepository messages,ReviewRepository reviews,PasswordEncoder encoder){
        this.users=users;this.roles=roles;this.freelancerProfiles=freelancerProfiles;this.clientProfiles=clientProfiles;this.skills=skills;this.projects=projects;
        this.proposals=proposals;this.assignments=assignments;this.milestones=milestones;this.submissions=submissions;this.payments=payments;
        this.notifications=notifications;this.conversations=conversations;this.messages=messages;this.reviews=reviews;this.encoder=encoder;
    }
    @Override @Transactional
    public void run(ApplicationArguments args){
        if(users.count()>0)return;
        Role clientRole=role("CLIENT"),freelancerRole=role("FREELANCER");
        List<UserAccount> clients=new ArrayList<>();
        for(int i=1;i<=5;i++){
            UserAccount u=account("client"+i+"@example.test","Client "+i,clientRole);clients.add(u);
            ClientProfile p=new ClientProfile();p.setUser(u);p.setCompanyName(List.of("Northstar Studio","Greenfield Labs","Blue Door Creative","Orbit Commerce","Fieldnote Works").get(i-1));p.setCompanyDescription("A small team building thoughtful digital products.");p.setWebsite("https://example.com");p.setLocation("Remote");clientProfiles.save(p);
        }
        List<Skill> allSkills=new ArrayList<>();for(String n:List.of("Java","Spring Boot","React","UX Design","Python","MySQL","Product Design","Node.js")){Skill s=new Skill();s.setName(n);allSkills.add(skills.save(s));}
        List<UserAccount> freelancers=new ArrayList<>();
        for(int i=1;i<=10;i++){
            UserAccount u=account("freelancer"+i+"@example.test","Freelancer "+i,freelancerRole);freelancers.add(u);
            FreelancerProfile p=new FreelancerProfile();p.setUser(u);p.setHeadline(List.of("Full stack engineer","Product designer","Backend engineer","Frontend developer").get((i-1)%4));p.setDescription("Independent professional with a track record of delivering reliable, well-documented work for distributed teams.");p.setHourlyRate(new BigDecimal(3500+i*500));p.setExperienceYears(2+i%8);p.setAvailability(i%5==0?"LIMITED":"AVAILABLE");p.setLocation("Remote");p.getSkills().add(allSkills.get((i-1)%allSkills.size()));p.getSkills().add(allSkills.get((i+1)%allSkills.size()));freelancerProfiles.save(p);
        }
        List<Project> projectList=new ArrayList<>();
        for(int i=1;i<=15;i++){
            Project p=new Project();p.setClient(clients.get((i-1)%clients.size()));
            if(i==3){
                p.setTitle("E-commerce Website Development");
                p.setDescription("Build a responsive e-commerce website with product listing, shopping cart and payment integration.");
                p.setProjectType(Project.ProjectType.FIXED_PRICE);p.setBudgetMin(new BigDecimal("30000.00"));p.setBudgetMax(new BigDecimal("50000.00"));p.setExperienceLevel("INTERMEDIATE");p.setCategory("E-commerce");p.setDeadline(LocalDate.now().plusDays(30));
                p.getRequiredSkills().addAll(List.of(allSkills.get(0),allSkills.get(2),allSkills.get(5)));
            }else{
                p.setTitle(List.of("Build a booking dashboard","Improve product onboarding","Create a client portal","Develop a reporting API","Design a commerce experience").get((i-1)%5)+" "+i);
                p.setDescription("We need an experienced professional to help us deliver a clear, dependable product experience. Scope includes implementation, review, and handoff documentation.");
                p.setProjectType(i%3==0?Project.ProjectType.HOURLY:Project.ProjectType.FIXED_PRICE);p.setBudgetMin(new BigDecimal("1200.00"));p.setBudgetMax(new BigDecimal("4200.00"));p.setExperienceLevel(i%3==0?"EXPERT":"INTERMEDIATE");p.setCategory(i%2==0?"Web development":"Product design");p.setDeadline(LocalDate.now().plusDays(30+i));p.getRequiredSkills().add(allSkills.get((i-1)%allSkills.size()));p.getRequiredSkills().add(allSkills.get((i+1)%allSkills.size()));
            }
            p.setStatus(Project.ProjectStatus.OPEN);projectList.add(projects.save(p));
        }
        for(int i=0;i<projectList.size();i++){
            Project p=projectList.get(i);
            for(int j=0;j<3;j++){
                UserAccount freelancer=freelancers.get((i+j)%freelancers.size());Proposal proposal=new Proposal();proposal.setProject(p);proposal.setFreelancer(freelancer);proposal.setCoverLetter("I have relevant experience and can deliver a clear, maintainable solution. I will share progress at each agreed milestone.");proposal.setProposedBudget(new BigDecimal(i==2?"40000.00":"2400.00"));proposal.setCompletionDays(24+j*4);proposal.setStatus(Proposal.ProposalStatus.PENDING);proposal.getPortfolioLinks().add("https://example.com/portfolio/"+(i+j+1));proposals.save(proposal);
            }
        }
        List<Proposal> firstProjectProposals=proposals.findByProjectId(projectList.get(0).getId());firstProjectProposals.stream().skip(1).forEach(p->p.setStatus(Proposal.ProposalStatus.REJECTED));
        Assignment active=assign(projectList.get(0),freelancers.get(0),clients.get(0),firstProjectProposals.get(0));
        projectList.get(0).setStatus(Project.ProjectStatus.IN_PROGRESS);
        Milestone done=milestone(active,"Research and structure",1,"APPROVED",new BigDecimal("799.99"));
        MilestoneSubmission sub=new MilestoneSubmission();sub.setMilestone(done);sub.setSubmittedBy(freelancers.get(0));sub.setDescription("Completed the discovery and page structure handoff.");sub.setComments("Includes notes for the next phase.");submissions.save(sub);
        Payment payment=new Payment();payment.setAssignment(active);payment.setMilestone(done);payment.setAmount(done.getAmount());payment.setStatus(Payment.PaymentStatus.PENDING);payments.save(payment);
        milestone(active,"Dashboard implementation",2,"IN_PROGRESS",new BigDecimal("1600.00"));
        Milestone reviewMilestone=milestone(active,"Client review",3,"SUBMITTED",new BigDecimal("0.01"));
        MilestoneSubmission reviewSubmission=new MilestoneSubmission();reviewSubmission.setMilestone(reviewMilestone);reviewSubmission.setSubmittedBy(freelancers.get(0));reviewSubmission.setDescription("Preview build and review notes are ready.");submissions.save(reviewSubmission);
        List<Proposal> secondProjectProposals=proposals.findByProjectId(projectList.get(1).getId());secondProjectProposals.stream().skip(1).forEach(p->p.setStatus(Proposal.ProposalStatus.REJECTED));
        Assignment completed=assign(projectList.get(1),freelancers.get(1),clients.get(1),secondProjectProposals.get(0));
        completed.setStatus(Assignment.AssignmentStatus.COMPLETED);completed.setCompletedAt(Instant.now());projectList.get(1).setStatus(Project.ProjectStatus.COMPLETED);
        Milestone completedMilestone=milestone(completed,"Website launch",1,"APPROVED",new BigDecimal("2400.00"));
        Review review=new Review();review.setAssignment(completed);review.setReviewer(clients.get(1));review.setReviewee(freelancers.get(1));review.setRating(5);review.setFeedback("Clear communication and a polished handoff.");reviews.save(review);
        Conversation conversation=new Conversation();conversation.setProject(projectList.get(0));conversation.getParticipants().add(clients.get(0));conversation.getParticipants().add(freelancers.get(0));conversation=conversations.save(conversation);
        Message message=new Message();message.setConversation(conversation);message.setSender(clients.get(0));message.setBody("Welcome. I have added the milestone plan; let me know if you need any clarification.");messages.save(message);
        notify(clients.get(0),"PROPOSAL_RECEIVED","PROJECT",projectList.get(0).getId(),"New proposals are waiting for review.");
        notify(freelancers.get(0),"WORK_APPROVED","MILESTONE",done.getId(),"The discovery milestone was approved; payment is pending.");
    }
    private Role role(String name){return roles.findByName(name).orElseThrow();}
    private UserAccount account(String email,String name,Role role){UserAccount u=new UserAccount();u.setEmail(email);u.setFullName(name);u.setPasswordHash(encoder.encode(DEMO_PASSWORD));u.getRoles().add(role);return users.save(u);}
    private Assignment assign(Project p,UserAccount freelancer,UserAccount client,Proposal proposal){proposal.setStatus(Proposal.ProposalStatus.ACCEPTED);Assignment a=new Assignment();a.setProject(p);a.setProposal(proposal);a.setClient(client);a.setFreelancer(freelancer);return assignments.save(a);}
    private Milestone milestone(Assignment a,String title,int sequence,String status,BigDecimal amount){Milestone m=new Milestone();m.setAssignment(a);m.setTitle(title);m.setDescription("A defined deliverable with client review and written handoff.");m.setSequenceNumber(sequence);m.setDueDate(LocalDate.now().plusDays(10+sequence));m.setAmount(amount);m.setStatus(Milestone.MilestoneStatus.valueOf(status));return milestones.save(m);}
    private void notify(UserAccount user,String type,String resourceType,Long resourceId,String text){Notification n=new Notification();n.setUser(user);n.setType(type);n.setResourceType(resourceType);n.setResourceId(resourceId);n.setMessage(text);notifications.save(n);}
}
