package com.example.SocialMediaApp.Scheduling.application;

import com.example.SocialMediaApp.Notification.application.ContentNotificationService;
import com.example.SocialMediaApp.Notification.domain.ContentEmail;
import com.example.SocialMediaApp.Scheduling.Jobs.PublishPostJob;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentSchedulingService {

    private final Scheduler scheduler;
    private final ContentNotificationService contentNotificationService;
    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;

    public void schedulePostPublishing(String postId,Instant scheduledAt) throws SchedulerException {
        JobDetail jobDetail=JobBuilder.newJob(PublishPostJob.class).
                withIdentity("schedule-publish-post"+postId).
                withDescription("Publishing Scheduled Posts").storeDurably().
                usingJobData("postId",postId).
                build();
        Trigger trigger=TriggerBuilder.newTrigger().
                withIdentity("trigger-" + postId, "post-triggers")
                .startAt(Date.from(scheduledAt)).forJob(jobDetail)
                .build();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (SchedulerException e) {
                    log.error("");
                }
            }
        });

        // only send schedule email if post is scheduler 24h ahead
        if(scheduledAt.isAfter(Instant.now().plus(24,ChronoUnit.HOURS))){
            String currentUserId=authenticatedUserService.getCurrentUser();
            User user=userRepo.findById(currentUserId).orElseThrow();
            List<Map<String,String>> to=List.of(Map.of("email",user.getEmail()));
            String at =scheduledAt.minus(12, ChronoUnit.HOURS).toString();
            ContentEmail emailSending= new ContentEmail(to,"Reminder: Your post is going live soon",postId,at);
            contentNotificationService.sendEmail(emailSending);
            log.info("Scheduling done for post {}", postId);
        }

    }

    public void unSchedulePostPublishing(String postId) throws SchedulerException {
        JobKey jobKey=JobKey.jobKey("schedule-publish-post"+postId);
        if(scheduler.deleteJob(jobKey)) log.info("unScheduling post : "+postId);
        else log.warn("failed unScheduling post : {}", postId);
    }


}
