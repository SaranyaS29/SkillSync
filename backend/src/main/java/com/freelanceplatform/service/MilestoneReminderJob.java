package com.freelanceplatform.service;

import com.freelanceplatform.entity.Milestone;
import com.freelanceplatform.repository.MilestoneRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component
public class MilestoneReminderJob {
    private final MilestoneRepository milestones; private final NotificationService notifications;
    public MilestoneReminderJob(MilestoneRepository milestones,NotificationService notifications){this.milestones=milestones;this.notifications=notifications;}
    @Scheduled(cron="0 0 8 * * *",zone="UTC") @Transactional
    public void remindDueTomorrow(){
        LocalDate due=LocalDate.now(java.time.ZoneOffset.UTC).plusDays(1);
        for(Milestone m:milestones.findByDueDateAndStatusIn(due,List.of(Milestone.MilestoneStatus.PENDING,Milestone.MilestoneStatus.IN_PROGRESS,Milestone.MilestoneStatus.REVISION_REQUESTED))){
            String message="Milestone '"+m.getTitle()+"' is due tomorrow for project '"+m.getAssignment().getProject().getTitle()+"'.";
            notifications.notify(m.getAssignment().getClient(),"DEADLINE_REMINDER","MILESTONE",m.getId(),message);
            notifications.notify(m.getAssignment().getFreelancer(),"DEADLINE_REMINDER","MILESTONE",m.getId(),message);
        }
    }
}
